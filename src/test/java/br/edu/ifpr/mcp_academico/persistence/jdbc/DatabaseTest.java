package br.edu.ifpr.mcp_academico.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DatabaseTest extends DatabaseTestSupport {
   private int alunos() {
      return database.execute(session -> session.queryOne(
                  "SELECT COUNT(*) FROM aluno",row -> row.getInt(1))
                                                .orElseThrow());
   }

   private void inserir(SqlSession session, String ra) {
      session.update("INSERT INTO aluno (ra, nome, email) VALUES (?, ?, ?)",
                     ra,"Ana",ra + "@example.com");
   }

   @Test
   void deveConfirmarATransacaoAoTerminar() {
      database.transaction(session -> {
         inserir(session,"1");
         inserir(session,"2");
         return null;
      });

      assertEquals(2,alunos());
   }

   @Test
   void deveDesfazerATransacaoEmCasoDeErro() {
      assertThrows(IllegalStateException.class,
                   () -> database.transaction(session -> {
         inserir(session,"1");
         throw new IllegalStateException("falha no meio da transação");
      }));

      assertEquals(0,alunos());
   }

   @Test
   void deveConverterFalhaDeSqlEmPersistenceException() {
      assertThrows(PersistenceException.class,
                   () -> database.execute(session -> session.update("NADA")));
   }

   @Test
   void deveConverterFalhaDeConsultaEmPersistenceException() {
      assertThrows(PersistenceException.class,
                   () -> database.execute(session ->
                            session.query("SELECT * FROM nada",row -> 1)));
   }

   @Test
   void deveConverterFalhaDeInsercaoEmPersistenceException() {
      assertThrows(PersistenceException.class,
                   () -> database.execute(session ->
                            session.insert("INSERT INTO nada VALUES (1)")));
   }

   @Test
   void deveImpedirReferenciaInexistente() {
      assertThrows(PersistenceException.class,
                   () -> database.execute(session -> session.update(
                     "INSERT INTO matricula " +
                     "(aluno_id, disciplina_id, semestre, status) " +
                     "VALUES (9, 9, '2026/1', 'ATIVA')")));
   }

   @Test
   void deveFalharSeOArquivoNaoPuderSerAberto() throws Exception {
      Path     missing = directory.resolve("nao").resolve("existe.db");
      Database broken  = new Database(new ConnectionFactory(missing));

      assertThrows(PersistenceException.class,
                   () -> broken.execute(session -> 1));
      assertThrows(PersistenceException.class,
                   () -> broken.transaction(session -> 1));
      assertTrue(Files.notExists(missing));
   }
}
