package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
import br.edu.ifpr.mcp_academico.domain.model.ClassificacaoTurma;
import br.edu.ifpr.mcp_academico.domain.model.EstatisticasTurma;
import br.edu.ifpr.mcp_academico.domain.model.HistoricoAluno;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

class RelatorioServiceTest {
   private static final String    SEMESTRE = "2026/1";
   private static final LocalDate AULA     = LocalDate.of(2026,3,2);

   private final InMemoryRepositories.Alunos      alunos      =
                                            new InMemoryRepositories.Alunos();
   private final InMemoryRepositories.Disciplinas disciplinas =
                                       new InMemoryRepositories.Disciplinas();
   private final InMemoryRepositories.Matriculas  matriculas  =
                                        new InMemoryRepositories.Matriculas();
   private final InMemoryRepositories.Avaliacoes  avaliacoes  =
                                        new InMemoryRepositories.Avaliacoes();
   private final InMemoryRepositories.Frequencias frequencias =
                                       new InMemoryRepositories.Frequencias();
   private final RelatorioService                 service     =
         new RelatorioService(alunos,disciplinas,matriculas,avaliacoes,
                              frequencias,new CalculadoraSituacao());

   private static void assertCode(ErrorCode code, Executable action) {
      assertEquals(code,assertThrows(DomainException.class,action).code());
   }

   // grava um aluno matriculado, com nota e frequência informadas
   private void aluno(int id, String nome, Double nota, Boolean presente) {
      alunos.save(String.valueOf(id),nome,id + "@example.com");

      Matricula matricula = matriculas.save(id,1,SEMESTRE);

      if (nota != null)
         avaliacoes.saveWithNotas(1,SEMESTRE,"Prova " + id,1,
                                  List.of(new NotaMatricula(matricula.id(),
                                                            nota)));
      if (presente != null)
         frequencias.save(AULA,Map.of(matricula.id(),presente));
   }

   @BeforeEach
   void prepararTurma() {
      disciplinas.save("SWEB","Serviços Web",80);
      disciplinas.save("BD","Banco de Dados",80);
      aluno(1,"Ana",9.0,true);
      aluno(2,"Bia",7.0,true);
      aluno(3,"Caio",4.0,true);
      aluno(4,"Davi",8.0,false);
      aluno(5,"Eva",null,null);
   }

   // ============================= situação ===============================

   @Test
   void deveCalcularASituacaoDoAluno() {
      ResultadoAcademico resultado = service.situacao(1,1,SEMESTRE);

      assertEquals(9.0,resultado.media());
      assertEquals(100.0,resultado.frequencia());
      assertEquals(Situacao.APROVADO,resultado.situacao());
   }

   @Test
   void deveRejeitarSituacaoSemMatricula() {
      assertCode(ErrorCode.MATRICULA_NAO_ENCONTRADA,
                 () -> service.situacao(1,2,SEMESTRE));
   }

   @Test
   void deveRejeitarSituacaoComSemestreInvalido() {
      assertCode(ErrorCode.SEMESTRE_INVALIDO,
                 () -> service.situacao(1,1,"2026"));
   }

   // ============================== boletim ===============================

   @Test
   void deveGerarOBoletimDoSemestre() {
      Boletim boletim = service.boletim(1,SEMESTRE);

      assertEquals("Ana",boletim.aluno().nome());
      assertEquals(1,boletim.disciplinas().size());
      assertEquals("SWEB",boletim.disciplinas().get(0).codigo());
      assertEquals(1,boletim.disciplinas().get(0).notas().size());
      assertEquals(Situacao.APROVADO,
                   boletim.disciplinas().get(0).resultado().situacao());
   }

   @Test
   void deveIgnorarMatriculasDeOutrosSemestresNoBoletim() {
      matriculas.save(1,2,"2026/2");

      assertEquals(1,service.boletim(1,SEMESTRE).disciplinas().size());
   }

   @Test
   void deveRejeitarBoletimDeAlunoInexistente() {
      assertCode(ErrorCode.ALUNO_NAO_ENCONTRADO,
                 () -> service.boletim(99,SEMESTRE));
   }

   @Test
   void deveRejeitarBoletimSemMatriculas() {
      assertCode(ErrorCode.SEM_MATRICULAS,() -> service.boletim(1,"2025/1"));
   }

   // ============================= histórico ==============================

   @Test
   void deveReunirTodasAsMatriculasNoHistorico() {
      matriculas.save(1,2,"2026/2");

      HistoricoAluno historico = service.historico(1);

      assertEquals(2,historico.disciplinas().size());
   }

   @Test
   void deveRejeitarHistoricoDeAlunoInexistente() {
      assertCode(ErrorCode.ALUNO_NAO_ENCONTRADO,() -> service.historico(99));
   }

   @Test
   void deveIncluirMatriculaCanceladaNoHistorico() {
      matriculas.updateStatus(1,StatusMatricula.CANCELADA);

      assertEquals(Situacao.CANCELADA,
                   service.historico(1).disciplinas().get(0).resultado()
                          .situacao());
   }

   // ============================== ranking ===============================

   @Test
   void deveOrdenarPelaMediaIgnorandoAlunosSemNota() {
      List<ClassificacaoTurma> ranking = service.ranking(1,SEMESTRE,10);

      assertEquals(List.of("Ana","Davi","Bia","Caio"),
                   ranking.stream().map(ClassificacaoTurma::nome).toList());
      assertEquals(List.of(1,2,3,4),
                   ranking.stream().map(ClassificacaoTurma::posicao).toList());
   }

   @Test
   void deveDesempatarPeloNome() {
      aluno(6,"Abel",9.0,true);

      assertEquals("Abel",service.ranking(1,SEMESTRE,1).get(0).nome());
   }

   @Test
   void deveRespeitarOLimiteDoRanking() {
      assertEquals(2,service.ranking(1,SEMESTRE,2).size());
   }

   @Test
   void deveRejeitarLimiteInvalidoNoRanking() {
      assertCode(ErrorCode.LIMITE_INVALIDO,
                 () -> service.ranking(1,SEMESTRE,0));
      assertCode(ErrorCode.LIMITE_INVALIDO,
                 () -> service.ranking(1,SEMESTRE,101));
   }

   @Test
   void deveRejeitarRankingDeDisciplinaInexistente() {
      assertCode(ErrorCode.DISCIPLINA_NAO_ENCONTRADA,
                 () -> service.ranking(9,SEMESTRE,10));
   }

   @Test
   void deveRejeitarRankingDeTurmaVazia() {
      assertCode(ErrorCode.TURMA_VAZIA,() -> service.ranking(2,SEMESTRE,10));
   }

   // ============================ estatísticas ============================

   @Test
   void deveCalcularEstatisticasDaTurma() {
      EstatisticasTurma estatisticas = service.estatisticas(1,SEMESTRE);

      assertEquals(5,estatisticas.alunos());
      assertEquals(4,estatisticas.avaliados());
      assertEquals(7.0,estatisticas.media());
      assertEquals(9.0,estatisticas.maiorMedia());
      assertEquals(4.0,estatisticas.menorMedia());
      assertEquals(1.87,estatisticas.desvioPadrao());
      assertEquals(2,estatisticas.aprovados());
      assertEquals(1,estatisticas.reprovadosNota());
      assertEquals(1,estatisticas.reprovadosFrequencia());
      assertEquals(1,estatisticas.emCurso());
      assertEquals(50.0,estatisticas.taxaAprovacao());
   }

   @Test
   void deveRetornarValoresNulosEmTurmaSemLancamentos() {
      matriculas.save(1,2,SEMESTRE);

      EstatisticasTurma estatisticas = service.estatisticas(2,SEMESTRE);

      assertEquals(1,estatisticas.alunos());
      assertEquals(0,estatisticas.avaliados());
      assertNull(estatisticas.media());
      assertNull(estatisticas.desvioPadrao());
      assertNull(estatisticas.taxaAprovacao());
   }

   @Test
   void deveRejeitarEstatisticasDeTurmaVazia() {
      assertCode(ErrorCode.TURMA_VAZIA,
                 () -> service.estatisticas(2,SEMESTRE));
   }
}
