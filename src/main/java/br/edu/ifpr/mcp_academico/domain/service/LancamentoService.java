// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa as classes do Java para trabalhar com datas
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
// importa as classes do Java para trabalhar com listas e mapas (um mapa
// liga uma chave a um valor, como o ID do aluno à sua matrícula)
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// importa o erro da aplicação, os códigos de erro e as classes de dados
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.NotaAluno;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.model.Semestre;
// importa os repositórios (classes que leem e gravam no banco de dados)
import br.edu.ifpr.mcp_academico.persistence.AvaliacaoRepository;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.FrequenciaRepository;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;

// regras para lançar notas e registrar a frequência de uma turma
public class LancamentoService {
   // maior nota possível
   private static final int NOTA_MAXIMA = 10;

   // repositório das disciplinas
   private final DisciplinaRepository disciplinas;
   // repositório das matrículas
   private final MatriculaRepository  matriculas;
   // repositório das avaliações e notas
   private final AvaliacaoRepository  avaliacoes;
   // repositório das presenças
   private final FrequenciaRepository frequencias;
   // relógio usado para saber a data de hoje (nos testes, usa-se um relógio
   // com data fixa)
   private final Clock                relogio;

   // recebe os repositórios e o relógio que este serviço vai usar
   public LancamentoService(DisciplinaRepository disciplinas,
                            MatriculaRepository matriculas,
                            AvaliacaoRepository avaliacoes,
                            FrequenciaRepository frequencias, Clock relogio) {
      // guarda cada um deles para usar nos outros métodos
      this.disciplinas = disciplinas;
      this.matriculas  = matriculas;
      this.avaliacoes  = avaliacoes;
      this.frequencias = frequencias;
      this.relogio     = relogio;
   }

   // lança as notas de uma avaliação para vários alunos de uma turma e
   // devolve quantas notas foram gravadas
   public int lancarNotas(int disciplinaId, String semestre, String avaliacao,
                          double peso, List<NotaAluno> notas) {
      // confere o formato do semestre, como "2026/1"
      String periodo = Semestre.parse(semestre).toString();

      // confere se a disciplina existe
      verificarDisciplina(disciplinaId);
      // confere o peso e as notas
      validarNotas(peso,notas);

      // não deixa lançar duas vezes a mesma avaliação na turma
      if (avaliacoes.exists(disciplinaId,periodo,avaliacao))
         throw new DomainException(ErrorCode.AVALIACAO_DUPLICADA,
                                   "A avaliação '" + avaliacao + "' já foi " +
                                   "lançada nesta turma.");

      // busca os alunos matriculados na turma
      Map<Integer, Matricula> turma = turma(disciplinaId,periodo);
      // liga cada nota à matrícula do aluno
      List<NotaMatricula>     itens = notas.stream()
                                           .map(nota -> vincular(nota,turma))
                                           .toList();

      // grava a avaliação e todas as notas de uma só vez
      avaliacoes.saveWithNotas(disciplinaId,periodo,avaliacao,peso,itens);

      // devolve a quantidade de notas gravadas
      return itens.size();
   }

   // registra a chamada de um dia: todos presentes, menos os ausentes.
   // Devolve quantos alunos tiveram a presença registrada
   public int registrarFrequencia(int disciplinaId, String semestre,
                                  String data, List<Integer> ausentes) {
      // confere o formato do semestre
      String    periodo = Semestre.parse(semestre).toString();
      // confere e converte a data da aula
      LocalDate dia     = parseData(data);

      // confere se a disciplina existe
      verificarDisciplina(disciplinaId);

      // busca os alunos matriculados na turma
      Map<Integer, Matricula> turma = turma(disciplinaId,periodo);

      // não há chamada em uma turma sem alunos
      if (turma.isEmpty())
         throw new DomainException(ErrorCode.TURMA_VAZIA,
                                   "A turma não possui alunos matriculados.");

      // confere se cada ausente realmente está na turma
      ausentes.forEach(alunoId -> verificarMatriculado(alunoId,turma));
      // grava a presença ou a falta de cada aluno
      frequencias.save(dia,presencas(turma,ausentes));

      // devolve o número de alunos da turma
      return turma.size();
   }

   // confere o peso da avaliação e as notas informadas
   private void validarNotas(double peso, List<NotaAluno> notas) {
      // o peso precisa ser maior que zero
      if (peso <= 0)
         throw new DomainException(ErrorCode.PESO_INVALIDO,
                                   "O peso da avaliação deve ser positivo.");

      // precisa haver pelo menos uma nota
      if (notas.isEmpty())
         throw new DomainException(ErrorCode.ARGUMENTO_INVALIDO,
                                   "Informe ao menos uma nota.");

      // um mesmo aluno não pode aparecer duas vezes: conta os alunos sem
      // repetição e compara com o total de notas
      if (notas.stream().map(NotaAluno::alunoId).distinct().count() !=
          notas.size())
         throw new DomainException(ErrorCode.ARGUMENTO_INVALIDO,
                                   "Cada aluno deve ter somente uma nota.");

      // todas as notas precisam estar entre 0 e a nota máxima
      if (notas.stream().anyMatch(this::foraDaFaixa))
         throw new DomainException(ErrorCode.NOTA_FORA_DA_FAIXA,
                                   "As notas devem estar entre 0 e " +
                                   NOTA_MAXIMA + ".");
   }

   // responde "sim" (true) quando a nota é menor que 0 ou maior que 10
   private boolean foraDaFaixa(NotaAluno nota) {
      return nota.valor() < 0 || nota.valor() > NOTA_MAXIMA;
   }

   // troca o ID do aluno pela matrícula dele na turma
   private NotaMatricula vincular(NotaAluno nota,
                                  Map<Integer, Matricula> turma) {
      // confere se o aluno está na turma
      verificarMatriculado(nota.alunoId(),turma);

      // cria a nota ligada ao ID da matrícula do aluno
      return new NotaMatricula(turma.get(nota.alunoId()).id(),nota.valor());
   }

   // avisa um erro se o aluno não estiver matriculado na turma
   private void verificarMatriculado(int alunoId,
                                     Map<Integer, Matricula> turma) {
      // procura o aluno no mapa da turma
      if (!turma.containsKey(alunoId))
         throw new DomainException(ErrorCode.ALUNO_NAO_MATRICULADO,
                                   "O aluno " + alunoId + " não está " +
                                   "matriculado nesta turma.");
   }

   // avisa um erro se a disciplina não existir
   private void verificarDisciplina(int disciplinaId) {
      // procura a disciplina no banco; "isEmpty" indica que não achou
      if (disciplinas.findById(disciplinaId).isEmpty())
         throw new DomainException(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                                   "Não existe a disciplina " + disciplinaId +
                                   ".");
   }

   // monta um mapa da turma: para cada ID de aluno, a sua matrícula ativa
   private Map<Integer, Matricula> turma(int disciplinaId, String semestre) {
      // busca as matrículas ativas e transforma a lista em mapa
      return matriculas.findAtivasDaTurma(disciplinaId,semestre)
                       .stream()
                       .collect(Collectors.toMap(Matricula::alunoId,
                                                 Function.identity()));
   }

   // monta um mapa com a presença de cada matrícula: true para presente e
   // false para ausente
   private Map<Integer, Boolean> presencas(Map<Integer, Matricula> turma,
                                           List<Integer> ausentes) {
      // cria o mapa vazio, que mantém a ordem de inserção
      Map<Integer, Boolean> presencas = new LinkedHashMap<>();

      // para cada aluno da turma, marca presente se ele não estiver na lista
      // de ausentes
      turma.forEach((alunoId,matricula) ->
                     presencas.put(matricula.id(),!ausentes.contains(alunoId)));

      // devolve o mapa pronto
      return presencas;
   }

   // converte o texto da data (AAAA-MM-DD) em uma data do Java
   private LocalDate parseData(String data) {
      // variável que vai guardar a data convertida
      LocalDate dia;

      // tenta converter o texto; se o formato estiver errado, avisa o erro
      try { dia = LocalDate.parse(data); }
      catch (DateTimeParseException ex) {
         throw new DomainException(ErrorCode.DATA_INVALIDA,
                                   "A data '" + data + "' deve estar no " +
                                   "formato AAAA-MM-DD.");
      }

      // não aceita registrar presença em um dia que ainda não chegou
      if (dia.isAfter(LocalDate.now(relogio)))
         throw new DomainException(ErrorCode.DATA_INVALIDA,
                                   "Não é possível registrar frequência em " +
                                   "uma data futura.");

      // devolve a data convertida
      return dia;
   }
}
