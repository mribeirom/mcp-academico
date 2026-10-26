package br.edu.ifpr.mcp_academico.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.Situacao;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;

class SchemaInitializerTest extends DatabaseTestSupport {
   private int count(String table) {
      return database.execute(session -> session.queryOne(
                  "SELECT COUNT(*) FROM " + table,row -> row.getInt(1))
                                                .orElseThrow());
   }

   @Test
   void deveCriarOSchemaVazio() {
      assertEquals(0,count("aluno"));
   }

   @Test
   void devePoderSerExecutadoNovamente() {
      SchemaInitializer initializer = new SchemaInitializer(database);

      initializer.createSchema();
      initializer.seedIfEmpty();
      initializer.createSchema();
      initializer.seedIfEmpty();

      assertEquals(8,count("aluno"));
   }

   @Test
   void naoDeveAlterarUmBancoQueJaPossuiDados() {
      database.execute(session -> session.update(
            "INSERT INTO aluno (ra, nome, email) VALUES ('1', 'Ana', 'a@b.c')"));

      new SchemaInitializer(database).seedIfEmpty();

      assertEquals(1,count("aluno"));
   }

   @Test
   void osDadosDeExemploDevemCobrirTodasAsSituacoes() {
      new SchemaInitializer(database).seedIfEmpty();

      RelatorioService relatorios = new RelatorioService(
                            new JdbcAlunoRepository(database),
                            new JdbcDisciplinaRepository(database),
                            new JdbcMatriculaRepository(database),
                            new JdbcAvaliacaoRepository(database),
                            new JdbcFrequenciaRepository(database),
                            new CalculadoraSituacao());

      assertEquals(Situacao.APROVADO,
                   relatorios.situacao(1,1,"2026/1").situacao());
      assertEquals(Situacao.REPROVADO_NOTA,
                   relatorios.situacao(3,1,"2026/1").situacao());
      assertEquals(Situacao.REPROVADO_FREQUENCIA,
                   relatorios.situacao(4,1,"2026/1").situacao());
      assertEquals(Situacao.CANCELADA,
                   relatorios.situacao(6,1,"2026/1").situacao());
      assertEquals(Situacao.EM_CURSO,
                   relatorios.situacao(1,1,"2026/2").situacao());
   }

   @Test
   void osDadosDeExemploDevemPreservarAcentos() {
      new SchemaInitializer(database).seedIfEmpty();

      assertEquals("Ana Luíza Gonçalves",
                   new JdbcAlunoRepository(database).findById(1).orElseThrow()
                                                    .nome());
      assertTrue(new JdbcMatriculaRepository(database).findById(6).orElseThrow()
                                                      .status() ==
                 StatusMatricula.CANCELADA);
   }
}
