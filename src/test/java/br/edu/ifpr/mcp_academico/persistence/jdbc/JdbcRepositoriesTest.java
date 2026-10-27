package br.edu.ifpr.mcp_academico.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

class JdbcRepositoriesTest extends DatabaseTestSupport {
   private JdbcAlunoRepository      alunos;
   private JdbcDisciplinaRepository disciplinas;
   private JdbcMatriculaRepository  matriculas;
   private JdbcAvaliacaoRepository  avaliacoes;
   private JdbcFrequenciaRepository frequencias;

   private Aluno      ana;
   private Disciplina sweb;
   private Matricula  matricula;

   @BeforeEach
   void prepararDados() {
      alunos      = new JdbcAlunoRepository(database);
      disciplinas = new JdbcDisciplinaRepository(database);
      matriculas  = new JdbcMatriculaRepository(database);
      avaliacoes  = new JdbcAvaliacaoRepository(database);
      frequencias = new JdbcFrequenciaRepository(database);

      ana       = alunos.save("2026001","Ana Conceição","ana@example.com");
      sweb      = disciplinas.save("SWEB","Serviços Web",80);
      matricula = matriculas.save(ana.id(),sweb.id(),"2026/2");
   }

   // ============================== alunos ================================

   @Test
   void deveGravarERecuperarAlunoComAcentos() {
      assertEquals(Optional.of(ana),alunos.findById(ana.id()));
   }

   @Test
   void deveRetornarVazioParaAlunoInexistente() {
      assertEquals(Optional.empty(),alunos.findById(999));
   }

   @Test
   void deveVerificarRaEEmailExistentes() {
      assertTrue(alunos.existsByRa("2026001"));
      assertFalse(alunos.existsByRa("0"));
      assertTrue(alunos.existsByEmail("ana@example.com"));
      assertFalse(alunos.existsByEmail("x@example.com"));
   }

   @Test
   void deveImpedirRaDuplicadoNoBanco() {
      assertThrows(PersistenceException.class,
                   () -> alunos.save("2026001","Outra","outra@example.com"));
   }

   @Test
   void deveBuscarPorNomeRaOuEmailEmOrdemAlfabetica() {
      alunos.save("2026002","Bruno Conceição","bruno@example.com");

      assertEquals(List.of("Ana Conceição","Bruno Conceição"),
                   alunos.search("Conceição",10).stream().map(Aluno::nome)
                         .toList());
      assertEquals(1,alunos.search("2026002",10).size());
      assertEquals(1,alunos.search("bruno@",10).size());
      assertEquals(1,alunos.search("Conceição",1).size());
      assertEquals(List.of(),alunos.search("Zé",10));
   }

   // =========================== disciplinas ==============================

   @Test
   void deveGravarERecuperarDisciplina() {
      assertEquals(Optional.of(sweb),disciplinas.findById(sweb.id()));
      assertEquals(Optional.empty(),disciplinas.findById(999));
      assertTrue(disciplinas.existsByCodigo("SWEB"));
      assertFalse(disciplinas.existsByCodigo("X"));
   }

   @Test
   void deveListarSomenteAsDisciplinasAtivas() {
      Disciplina inativa = disciplinas.save("LEG","Legislação",40);

      database.execute(session -> session.update(
                  "UPDATE disciplina SET ativa = 0 WHERE id = ?",inativa.id()));

      assertEquals(2,disciplinas.findAll(false).size());
      assertEquals(List.of(sweb),disciplinas.findAll(true));
   }

   // =========================== matrículas ===============================

   @Test
   void deveRecuperarMatriculaPorIdEPorChave() {
      assertEquals(Optional.of(matricula),matriculas.findById(matricula.id()));
      assertEquals(Optional.empty(),matriculas.findById(999));
      assertEquals(Optional.of(matricula),
                   matriculas.find(ana.id(),sweb.id(),"2026/2"));
      assertEquals(Optional.empty(),
                   matriculas.find(ana.id(),sweb.id(),"2026/1"));
   }

   @Test
   void deveImpedirMatriculaRepetidaNoBanco() {
      assertThrows(PersistenceException.class,
                   () -> matriculas.save(ana.id(),sweb.id(),"2026/2"));
   }

   @Test
   void deveListarAsMatriculasDoAluno() {
      assertEquals(List.of(matricula),matriculas.findByAluno(ana.id()));
   }

   @Test
   void deveListarSomenteAsMatriculasAtivasDaTurma() {
      assertEquals(List.of(matricula),
                   matriculas.findAtivasDaTurma(sweb.id(),"2026/2"));

      matriculas.updateStatus(matricula.id(),StatusMatricula.CANCELADA);

      assertEquals(List.of(),matriculas.findAtivasDaTurma(sweb.id(),"2026/2"));
      assertEquals(StatusMatricula.CANCELADA,
                   matriculas.findById(matricula.id()).orElseThrow().status());
   }

   // =========================== avaliações ===============================

   @Test
   void deveGravarAvaliacaoComNotas() {
      avaliacoes.saveWithNotas(sweb.id(),"2026/2","Prova 1",4,
                               List.of(new NotaMatricula(matricula.id(),8.5)));

      assertTrue(avaliacoes.exists(sweb.id(),"2026/2","Prova 1"));
      assertFalse(avaliacoes.exists(sweb.id(),"2026/1","Prova 1"));
      assertEquals(List.of(new NotaAvaliacao("Prova 1",4,8.5)),
                   avaliacoes.findNotas(matricula.id()));
   }

   @Test
   void naoDeveGravarNadaSeUmaNotaFalhar() {
      List<NotaMatricula> notas = List.of(new NotaMatricula(matricula.id(),8.5),
                                          new NotaMatricula(999,7.0));

      assertThrows(PersistenceException.class,
                   () -> avaliacoes.saveWithNotas(sweb.id(),"2026/2","Prova 1",
                                                  4,notas));
      assertFalse(avaliacoes.exists(sweb.id(),"2026/2","Prova 1"));
      assertEquals(List.of(),avaliacoes.findNotas(matricula.id()));
   }

   // =========================== frequências ==============================

   @Test
   void deveResumirAsAulasDaMatricula() {
      assertEquals(new ResumoFrequencia(0,0),
                   frequencias.resumo(matricula.id()));

      frequencias.save(LocalDate.of(2026,10,1),Map.of(matricula.id(),true));
      frequencias.save(LocalDate.of(2026,10,2),Map.of(matricula.id(),false));

      assertEquals(new ResumoFrequencia(2,1),
                   frequencias.resumo(matricula.id()));
   }

   @Test
   void deveSubstituirORegistroDaMesmaData() {
      LocalDate data = LocalDate.of(2026,10,1);

      frequencias.save(data,Map.of(matricula.id(),false));
      frequencias.save(data,Map.of(matricula.id(),true));

      assertEquals(new ResumoFrequencia(1,1),
                   frequencias.resumo(matricula.id()));
   }
}
