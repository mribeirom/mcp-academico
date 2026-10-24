package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

class MatriculaServiceTest {
   private final InMemoryRepositories.Alunos      alunos      =
                                            new InMemoryRepositories.Alunos();
   private final InMemoryRepositories.Disciplinas disciplinas =
                                       new InMemoryRepositories.Disciplinas();
   private final InMemoryRepositories.Matriculas  matriculas  =
                                        new InMemoryRepositories.Matriculas();
   private final MatriculaService                 service     =
                      new MatriculaService(alunos,disciplinas,matriculas);

   private static void assertCode(ErrorCode code, Executable action) {
      assertEquals(code,assertThrows(DomainException.class,action).code());
   }

   @BeforeEach
   void prepararDados() {
      alunos.save("1","Ana","ana@example.com");
      disciplinas.save("SWEB","Serviços Web",80);
   }

   @Test
   void deveMatricularAluno() {
      Matricula matricula = service.matricular(1,1,"2026/2");

      assertTrue(matricula.ativa());
      assertEquals("2026/2",matricula.semestre());
   }

   @Test
   void deveRejeitarSemestreInvalido() {
      assertCode(ErrorCode.SEMESTRE_INVALIDO,
                 () -> service.matricular(1,1,"2026"));
   }

   @Test
   void deveRejeitarAlunoInexistente() {
      assertCode(ErrorCode.ALUNO_NAO_ENCONTRADO,
                 () -> service.matricular(99,1,"2026/2"));
   }

   @Test
   void deveRejeitarDisciplinaInexistente() {
      assertCode(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                 () -> service.matricular(1,99,"2026/2"));
   }

   @Test
   void deveRejeitarDisciplinaInativa() {
      disciplinas.inativar(1);

      assertCode(ErrorCode.DISCIPLINA_INATIVA,
                 () -> service.matricular(1,1,"2026/2"));
   }

   @Test
   void deveRejeitarMatriculaDuplicada() {
      service.matricular(1,1,"2026/2");

      assertCode(ErrorCode.MATRICULA_DUPLICADA,
                 () -> service.matricular(1,1,"2026/2"));
   }

   @Test
   void devePermitirAMesmaDisciplinaEmOutroSemestre() {
      service.matricular(1,1,"2026/1");

      assertTrue(service.matricular(1,1,"2026/2").ativa());
   }

   @Test
   void deveReativarMatriculaCancelada() {
      Matricula original = service.matricular(1,1,"2026/2");

      service.cancelar(original.id());

      Matricula reativada = service.matricular(1,1,"2026/2");

      assertEquals(original.id(),reativada.id());
      assertTrue(reativada.ativa());
   }

   @Test
   void deveCancelarMatricula() {
      Matricula matricula = service.matricular(1,1,"2026/2");

      assertEquals(StatusMatricula.CANCELADA,
                   service.cancelar(matricula.id()).status());
      assertEquals(StatusMatricula.CANCELADA,
                   matriculas.findById(matricula.id()).orElseThrow().status());
   }

   @Test
   void deveRejeitarCancelamentoDeMatriculaInexistente() {
      assertCode(ErrorCode.MATRICULA_NAO_ENCONTRADA,
                 () -> service.cancelar(99));
   }

   @Test
   void deveRejeitarCancelamentoRepetido() {
      Matricula matricula = service.matricular(1,1,"2026/2");

      service.cancelar(matricula.id());

      assertCode(ErrorCode.MATRICULA_JA_CANCELADA,
                 () -> service.cancelar(matricula.id()));
   }
}
