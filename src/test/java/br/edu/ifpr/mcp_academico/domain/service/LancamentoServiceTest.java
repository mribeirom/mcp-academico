package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.NotaAluno;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;

class LancamentoServiceTest {
   private static final String SEMESTRE = "2026/2";
   private static final Clock  HOJE     =
         Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"),ZoneOffset.UTC);

   private final InMemoryRepositories.Disciplinas disciplinas =
                                       new InMemoryRepositories.Disciplinas();
   private final InMemoryRepositories.Matriculas  matriculas  =
                                        new InMemoryRepositories.Matriculas();
   private final InMemoryRepositories.Avaliacoes  avaliacoes  =
                                        new InMemoryRepositories.Avaliacoes();
   private final InMemoryRepositories.Frequencias frequencias =
                                       new InMemoryRepositories.Frequencias();
   private final LancamentoService                service     =
         new LancamentoService(disciplinas,matriculas,avaliacoes,frequencias,
                               HOJE);

   private static void assertCode(ErrorCode code, Executable action) {
      assertEquals(code,assertThrows(DomainException.class,action).code());
   }

   @BeforeEach
   void prepararTurma() {
      disciplinas.save("SWEB","Serviços Web",80);
      matriculas.save(1,1,SEMESTRE);
      matriculas.save(2,1,SEMESTRE);
   }

   // ============================== notas =================================

   @Test
   void deveLancarNotasDaTurma() {
      int lancadas = service.lancarNotas(1,SEMESTRE,"Prova 1",4,
                                         List.of(new NotaAluno(1,8.5),
                                                 new NotaAluno(2,7)));

      assertEquals(2,lancadas);
      assertEquals(List.of(new NotaAvaliacao("Prova 1",4,8.5)),
                   avaliacoes.findNotas(1));
   }

   @Test
   void deveRejeitarSemestreInvalidoNasNotas() {
      assertCode(ErrorCode.SEMESTRE_INVALIDO,
                 () -> service.lancarNotas(1,"x","P1",1,
                                           List.of(new NotaAluno(1,5))));
   }

   @Test
   void deveRejeitarDisciplinaInexistenteNasNotas() {
      assertCode(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                 () -> service.lancarNotas(9,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(1,5))));
   }

   @Test
   void deveRejeitarPesoInvalido() {
      assertCode(ErrorCode.PESO_INVALIDO,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",0,
                                           List.of(new NotaAluno(1,5))));
   }

   @Test
   void deveRejeitarListaDeNotasVazia() {
      assertCode(ErrorCode.ARGUMENTO_INVALIDO,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,List.of()));
   }

   @Test
   void deveRejeitarAlunoRepetidoNasNotas() {
      assertCode(ErrorCode.ARGUMENTO_INVALIDO,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(1,5),
                                                   new NotaAluno(1,6))));
   }

   @Test
   void deveRejeitarNotaForaDaFaixa() {
      assertCode(ErrorCode.NOTA_FORA_DA_FAIXA,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(1,10.5))));
      assertCode(ErrorCode.NOTA_FORA_DA_FAIXA,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(1,-1))));
   }

   @Test
   void deveRejeitarAvaliacaoDuplicada() {
      service.lancarNotas(1,SEMESTRE,"P1",1,List.of(new NotaAluno(1,5)));

      assertCode(ErrorCode.AVALIACAO_DUPLICADA,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(2,5))));
   }

   @Test
   void naoDeveGravarNadaSeUmAlunoNaoEstiverMatriculado() {
      assertCode(ErrorCode.ALUNO_NAO_MATRICULADO,
                 () -> service.lancarNotas(1,SEMESTRE,"P1",1,
                                           List.of(new NotaAluno(1,5),
                                                   new NotaAluno(99,5))));
      assertFalse(avaliacoes.exists(1,SEMESTRE,"P1"));
   }

   // =========================== frequência ===============================

   @Test
   void deveRegistrarPresencasEAusencias() {
      int registrados = service.registrarFrequencia(1,SEMESTRE,"2026-10-30",
                                                    List.of(2));

      assertEquals(2,registrados);
      assertEquals(new ResumoFrequencia(1,1),frequencias.resumo(1));
      assertEquals(new ResumoFrequencia(1,0),frequencias.resumo(2));
   }

   @Test
   void deveAceitarAulaNaDataDeHoje() {
      assertEquals(2,service.registrarFrequencia(1,SEMESTRE,"2026-10-31",
                                                 List.of()));
   }

   @Test
   void deveRejeitarDataEmFormatoInvalido() {
      assertCode(ErrorCode.DATA_INVALIDA,
                 () -> service.registrarFrequencia(1,SEMESTRE,"30/10/2026",
                                                   List.of()));
   }

   @Test
   void deveRejeitarDataInexistente() {
      assertCode(ErrorCode.DATA_INVALIDA,
                 () -> service.registrarFrequencia(1,SEMESTRE,"2026-02-30",
                                                   List.of()));
   }

   @Test
   void deveRejeitarDataFutura() {
      assertCode(ErrorCode.DATA_INVALIDA,
                 () -> service.registrarFrequencia(1,SEMESTRE,"2026-11-01",
                                                   List.of()));
   }

   @Test
   void deveRejeitarSemestreInvalidoNaFrequencia() {
      assertCode(ErrorCode.SEMESTRE_INVALIDO,
                 () -> service.registrarFrequencia(1,"x","2026-10-30",
                                                   List.of()));
   }

   @Test
   void deveRejeitarDisciplinaInexistenteNaFrequencia() {
      assertCode(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                 () -> service.registrarFrequencia(9,SEMESTRE,"2026-10-30",
                                                   List.of()));
   }

   @Test
   void deveRejeitarTurmaVaziaNaFrequencia() {
      assertCode(ErrorCode.TURMA_VAZIA,
                 () -> service.registrarFrequencia(1,"2020/1","2020-03-02",
                                                   List.of()));
   }

   @Test
   void deveRejeitarAusenteQueNaoEstaMatriculado() {
      assertCode(ErrorCode.ALUNO_NAO_MATRICULADO,
                 () -> service.registrarFrequencia(1,SEMESTRE,"2026-10-30",
                                                   List.of(99)));
      assertTrue(frequencias.resumo(1).aulas() == 0);
   }
}
