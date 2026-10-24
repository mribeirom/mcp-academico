// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa o Optional, uma "caixa" que pode ter um valor ou estar vazia
import java.util.Optional;

// importa o erro da aplicação, os códigos de erro e as classes de dados
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.Semestre;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;
// importa os repositórios (classes que leem e gravam no banco de dados)
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;

// regras para matricular alunos e cancelar matrículas
public class MatriculaService {
   // repositório dos alunos
   private final AlunoRepository      alunos;
   // repositório das disciplinas
   private final DisciplinaRepository disciplinas;
   // repositório das matrículas
   private final MatriculaRepository  matriculas;

   // recebe os repositórios que este serviço vai usar
   public MatriculaService(AlunoRepository alunos,
                           DisciplinaRepository disciplinas,
                           MatriculaRepository matriculas) {
      // guarda cada um deles para usar nos outros métodos
      this.alunos      = alunos;
      this.disciplinas = disciplinas;
      this.matriculas  = matriculas;
   }

   // matricula um aluno em uma disciplina, em um semestre
   public Matricula matricular(int alunoId, int disciplinaId, String semestre) {
      // confere o formato do semestre, como "2026/1"
      String periodo = Semestre.parse(semestre).toString();

      // confere se o aluno existe
      verificarAluno(alunoId);
      // confere se a disciplina existe e está ativa
      verificarDisciplinaAtiva(disciplinaId);

      // procura uma matrícula que já exista para esse aluno, disciplina e
      // semestre
      Optional<Matricula> existente =
                               matriculas.find(alunoId,disciplinaId,periodo);

      // se já existe, tenta reativá-la; senão, grava uma matrícula nova
      return existente.isPresent() ? reativar(existente.get())
                                   : matriculas.save(alunoId,disciplinaId,
                                                     periodo);
   }

   // cancela uma matrícula pelo seu ID
   public Matricula cancelar(int matriculaId) {
      // procura a matrícula; se não existir, avisa o erro
      Matricula matricula = matriculas.findById(matriculaId).orElseThrow(() ->
                  new DomainException(ErrorCode.MATRICULA_NAO_ENCONTRADA,
                                      "Não existe a matrícula " + matriculaId +
                                      "."));

      // não dá para cancelar uma matrícula já cancelada
      if (!matricula.ativa())
         throw new DomainException(ErrorCode.MATRICULA_JA_CANCELADA,
                                   "A matrícula " + matriculaId +
                                   " já está cancelada.");

      // muda o status para CANCELADA e devolve a matrícula atualizada
      return alterarStatus(matricula,StatusMatricula.CANCELADA);
   }

   // uma matrícula cancelada é reaproveitada, pois o aluno pode voltar à turma
   private Matricula reativar(Matricula matricula) {
      // se a matrícula ainda está ativa, o aluno já está na turma
      if (matricula.ativa())
         throw new DomainException(ErrorCode.MATRICULA_DUPLICADA,
                                   "O aluno já está matriculado nesta " +
                                   "disciplina neste semestre.");

      // muda o status de volta para ATIVA
      return alterarStatus(matricula,StatusMatricula.ATIVA);
   }

   // grava o novo status no banco e devolve a matrícula com esse status
   private Matricula alterarStatus(Matricula matricula, StatusMatricula status) {
      // atualiza o status no banco
      matriculas.updateStatus(matricula.id(),status);

      // cria uma cópia da matrícula com o status novo
      return new Matricula(matricula.id(),matricula.alunoId(),
                           matricula.disciplinaId(),matricula.semestre(),status);
   }

   // avisa um erro se o aluno não existir
   private void verificarAluno(int alunoId) {
      // procura o aluno no banco; "isEmpty" indica que não achou
      if (alunos.findById(alunoId).isEmpty())
         throw new DomainException(ErrorCode.ALUNO_NAO_ENCONTRADO,
                                   "Não existe o aluno " + alunoId + ".");
   }

   // avisa um erro se a disciplina não existir ou estiver inativa
   private void verificarDisciplinaAtiva(int disciplinaId) {
      // procura a disciplina; se não existir, avisa o erro
      Disciplina disciplina = disciplinas.findById(disciplinaId).orElseThrow(() ->
                  new DomainException(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                                      "Não existe a disciplina " + disciplinaId +
                                      "."));

      // disciplina inativa não aceita matrículas
      if (!disciplina.ativa())
         throw new DomainException(ErrorCode.DISCIPLINA_INATIVA,
                                   "A disciplina " + disciplina.codigo() +
                                   " não está ativa.");
   }
}
