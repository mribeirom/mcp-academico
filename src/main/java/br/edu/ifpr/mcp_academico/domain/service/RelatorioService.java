// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa as classes do Java para trabalhar com listas, ordenação e
// estatísticas
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;

// importa o erro da aplicação, os códigos de erro e as classes de dados
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
import br.edu.ifpr.mcp_academico.domain.model.ClassificacaoTurma;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.domain.model.EstatisticasTurma;
import br.edu.ifpr.mcp_academico.domain.model.HistoricoAluno;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoDisciplina;
import br.edu.ifpr.mcp_academico.domain.model.Semestre;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;
// importa os repositórios (classes que leem e gravam no banco de dados)
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;
import br.edu.ifpr.mcp_academico.persistence.AvaliacaoRepository;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.FrequenciaRepository;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;

// monta os relatórios: situação, boletim, histórico, ranking e estatísticas
public class RelatorioService {
   // maior quantidade de alunos que o ranking pode devolver
   private static final int LIMITE_MAXIMO = 100;

   // repositório dos alunos
   private final AlunoRepository      alunos;
   // repositório das disciplinas
   private final DisciplinaRepository disciplinas;
   // repositório das matrículas
   private final MatriculaRepository  matriculas;
   // repositório das avaliações e notas
   private final AvaliacaoRepository  avaliacoes;
   // repositório das presenças
   private final FrequenciaRepository frequencias;
   // classe que calcula média, frequência e situação
   private final CalculadoraSituacao  calculadora;

   // recebe os repositórios e a calculadora que este serviço vai usar
   public RelatorioService(AlunoRepository alunos,
                           DisciplinaRepository disciplinas,
                           MatriculaRepository matriculas,
                           AvaliacaoRepository avaliacoes,
                           FrequenciaRepository frequencias,
                           CalculadoraSituacao calculadora) {
      // guarda cada um deles para usar nos outros métodos
      this.alunos      = alunos;
      this.disciplinas = disciplinas;
      this.matriculas  = matriculas;
      this.avaliacoes  = avaliacoes;
      this.frequencias = frequencias;
      this.calculadora = calculadora;
   }

   // devolve a situação de um aluno em uma disciplina, em um semestre
   public ResultadoAcademico situacao(int alunoId, int disciplinaId,
                                      String semestre) {
      // confere o formato do semestre, como "2026/1"
      String periodo = Semestre.parse(semestre).toString();

      // procura a matrícula; se achar, calcula o resultado; se não achar,
      // avisa o erro
      return matriculas.find(alunoId,disciplinaId,periodo)
                       .map(this::resultado)
                       .orElseThrow(() -> new DomainException(
                             ErrorCode.MATRICULA_NAO_ENCONTRADA,
                             "O aluno " + alunoId + " não possui matrícula " +
                             "nesta disciplina em " + periodo + "."));
   }

   // monta o boletim de um aluno em um semestre
   public Boletim boletim(int alunoId, String semestre) {
      // confere o formato do semestre
      String                    periodo = Semestre.parse(semestre).toString();
      // busca o aluno (avisa o erro se ele não existir)
      Aluno                     aluno   = aluno(alunoId);
      // pega as matrículas do aluno, fica só com as do semestre pedido e
      // calcula o resultado de cada disciplina
      List<ResultadoDisciplina> linhas  =
                matriculas.findByAluno(alunoId)
                          .stream()
                          .filter(m -> m.semestre().equals(periodo))
                          .map(this::resultadoDisciplina)
                          .toList();

      // sem matrículas no semestre, não há boletim
      if (linhas.isEmpty())
         throw new DomainException(ErrorCode.SEM_MATRICULAS,
                                   "O aluno " + alunoId + " não possui " +
                                   "matrículas em " + periodo + ".");

      // junta o aluno, o semestre e as disciplinas no boletim
      return new Boletim(aluno,periodo,linhas);
   }

   // monta o histórico do aluno, com todas as disciplinas de todos os
   // semestres
   public HistoricoAluno historico(int alunoId) {
      // busca o aluno e calcula o resultado de cada matrícula dele
      return new HistoricoAluno(aluno(alunoId),
                                matriculas.findByAluno(alunoId)
                                          .stream()
                                          .map(this::resultadoDisciplina)
                                          .toList());
   }

   // monta o ranking da turma, da maior para a menor média
   public List<ClassificacaoTurma> ranking(int disciplinaId, String semestre,
                                           int limite) {
      // o limite precisa estar entre 1 e o máximo permitido
      if (limite < 1 || limite > LIMITE_MAXIMO)
         throw new DomainException(ErrorCode.LIMITE_INVALIDO,
                                   "O limite deve estar entre 1 e " +
                                   LIMITE_MAXIMO + ".");

      // para cada aluno da turma: calcula a classificação, descarta quem não
      // tem nota, ordena pela média (maior primeiro) e, em caso de empate,
      // pelo nome; por fim, fica só com os primeiros "limite" alunos
      List<ClassificacaoTurma> ordenados =
                turma(disciplinaId,semestre)
                          .stream()
                          .map(this::classificar)
                          .filter(Objects::nonNull)
                          .sorted(Comparator
                                    .comparingDouble(ClassificacaoTurma::media)
                                    .reversed()
                                    .thenComparing(ClassificacaoTurma::nome))
                          .limit(limite)
                          .toList();

      // coloca a posição (1º, 2º, 3º...) em cada aluno
      return numerar(ordenados);
   }

   // calcula os números que resumem o desempenho da turma
   public EstatisticasTurma estatisticas(int disciplinaId, String semestre) {
      // calcula o resultado de cada aluno da turma
      List<ResultadoAcademico> resultados = turma(disciplinaId,semestre)
                                                 .stream()
                                                 .map(this::resultado)
                                                 .toList();
      // separa as médias, descartando os alunos ainda sem nota
      List<Double> medias = resultados.stream()
                                      .map(ResultadoAcademico::media)
                                      .filter(Objects::nonNull)
                                      .toList();
      // conta quantos alunos estão em cada situação
      int aprovados        = contar(resultados,Situacao.APROVADO);
      int reprovadosNota   = contar(resultados,Situacao.REPROVADO_NOTA);
      int reprovadosFaltas = contar(resultados,Situacao.REPROVADO_FREQUENCIA);
      // alunos que já têm resultado final
      int concluidos       = aprovados + reprovadosNota + reprovadosFaltas;
      // o Java calcula de uma vez a média, a maior e a menor das médias
      DoubleSummaryStatistics resumo = medias.stream()
                                             .mapToDouble(Double::doubleValue)
                                             .summaryStatistics();
      // a turma foi avaliada se pelo menos um aluno tem média
      boolean avaliada = !medias.isEmpty();

      // monta as estatísticas; os valores que dependem de notas ficam vazios
      // (null) se a turma ainda não foi avaliada
      return new EstatisticasTurma(
                 resultados.size(),medias.size(),
                 avaliada ? arredondar(resumo.getAverage()) : null,
                 avaliada ? resumo.getMax() : null,
                 avaliada ? resumo.getMin() : null,
                 avaliada ? arredondar(desvioPadrao(medias,resumo)) : null,
                 aprovados,reprovadosNota,reprovadosFaltas,
                 contar(resultados,Situacao.EM_CURSO),
                 concluidos > 0 ? arredondar(aprovados * 100.0 / concluidos)
                                : null);
   }

   // busca as matrículas ativas de uma turma (disciplina + semestre)
   private List<Matricula> turma(int disciplinaId, String semestre) {
      // confere o formato do semestre
      String periodo = Semestre.parse(semestre).toString();

      // confere se a disciplina existe (avisa o erro se não existir)
      disciplina(disciplinaId);

      // busca as matrículas ativas da turma
      List<Matricula> turma = matriculas.findAtivasDaTurma(disciplinaId,periodo);

      // uma turma sem alunos não tem relatório
      if (turma.isEmpty())
         throw new DomainException(ErrorCode.TURMA_VAZIA,
                                   "A turma não possui alunos matriculados.");

      // devolve as matrículas encontradas
      return turma;
   }

   // calcula média, frequência e situação de uma matrícula
   private ResultadoAcademico resultado(Matricula matricula) {
      // busca as notas e as presenças e entrega para a calculadora
      return calculadora.calcular(matricula.status(),
                                  avaliacoes.findNotas(matricula.id()),
                                  frequencias.resumo(matricula.id()));
   }

   // monta a linha de uma disciplina no boletim ou no histórico
   private ResultadoDisciplina resultadoDisciplina(Matricula matricula) {
      // busca a disciplina da matrícula
      Disciplina          disciplina = disciplina(matricula.disciplinaId());
      // busca as notas do aluno nessa disciplina
      List<NotaAvaliacao> notas      = avaliacoes.findNotas(matricula.id());

      // junta os dados da disciplina, as notas e o resultado
      return new ResultadoDisciplina(disciplina.codigo(),disciplina.nome(),
                                     matricula.semestre(),notas,
                                     resultado(matricula));
   }

   // monta a linha do aluno no ranking. Alunos ainda sem notas não
   // participam da classificação, por isso devolvem null (vazio)
   private ClassificacaoTurma classificar(Matricula matricula) {
      // calcula o resultado do aluno
      ResultadoAcademico resultado = resultado(matricula);

      // sem média, o aluno fica fora do ranking
      if (resultado.media() == null)
         return null;

      // cria a linha com posição 0, que será trocada depois em "numerar"
      return new ClassificacaoTurma(0,matricula.alunoId(),
                                    aluno(matricula.alunoId()).nome(),
                                    resultado.media(),resultado.situacao());
   }

   // coloca a posição em cada linha do ranking, começando em 1
   private List<ClassificacaoTurma> numerar(List<ClassificacaoTurma> ordenados) {
      // lista nova que vai receber as linhas numeradas
      List<ClassificacaoTurma> numerados = new ArrayList<>();

      // para cada linha, a posição é o tamanho atual da lista mais 1
      for (ClassificacaoTurma item : ordenados)
         numerados.add(new ClassificacaoTurma(numerados.size() + 1,
                                              item.alunoId(),item.nome(),
                                              item.media(),item.situacao()));
      // devolve a lista numerada
      return numerados;
   }

   // conta quantos resultados estão em uma determinada situação
   private int contar(List<ResultadoAcademico> resultados, Situacao situacao) {
      // filtra os resultados com a situação pedida e conta quantos sobraram
      return (int) resultados.stream()
                             .filter(r -> r.situacao() == situacao)
                             .count();
   }

   // calcula o desvio padrão: quanto, em média, as notas se afastam da média
   // da turma
   private double desvioPadrao(List<Double> medias,
                               DoubleSummaryStatistics resumo) {
      // para cada média: diferença até a média da turma, elevada ao
      // quadrado; depois tira a média desses valores (a variância)
      double variancia = medias.stream()
                               .mapToDouble(media -> Math.pow(
                                         media - resumo.getAverage(),2))
                               .average()
                               .orElse(0);

      // o desvio padrão é a raiz quadrada da variância
      return Math.sqrt(variancia);
   }

   // arredonda um número para duas casas decimais
   private double arredondar(double valor) {
      // usa o mesmo arredondamento da calculadora
      return CalculadoraSituacao.arredondar(valor);
   }

   // busca um aluno pelo ID ou avisa o erro se ele não existir
   private Aluno aluno(int alunoId) {
      return alunos.findById(alunoId).orElseThrow(() ->
                new DomainException(ErrorCode.ALUNO_NAO_ENCONTRADO,
                                    "Não existe o aluno " + alunoId + "."));
   }

   // busca uma disciplina pelo ID ou avisa o erro se ela não existir
   private Disciplina disciplina(int disciplinaId) {
      return disciplinas.findById(disciplinaId).orElseThrow(() ->
                new DomainException(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                                    "Não existe a disciplina " + disciplinaId +
                                    "."));
   }
}
