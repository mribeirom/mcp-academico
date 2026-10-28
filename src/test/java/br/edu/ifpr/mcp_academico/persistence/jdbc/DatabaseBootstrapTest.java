package br.edu.ifpr.mcp_academico.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseBootstrapTest {
   @TempDir
   Path directory;

   @Test
   void deveCriarODiretorioEOsDadosDeExemplo() {
      Path     file     = directory.resolve("novo").resolve("academico.db");
      Database database = DatabaseBootstrap.open(file);
      int      alunos   = database.execute(session -> session.queryOne(
                  "SELECT COUNT(*) FROM aluno",row -> row.getInt(1))
                                                         .orElseThrow());

      assertTrue(Files.exists(file));
      assertEquals(8,alunos);
   }

   @Test
   void deveFalharSeODiretorioNaoPuderSerCriado() throws IOException {
      Path arquivo = Files.createFile(directory.resolve("arquivo"));

      assertThrows(UncheckedIOException.class,
                   () -> DatabaseBootstrap.open(arquivo.resolve("banco.db")));
   }
}
