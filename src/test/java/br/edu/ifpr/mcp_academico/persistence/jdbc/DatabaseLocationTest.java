package br.edu.ifpr.mcp_academico.persistence.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DatabaseLocationTest {
   @Test
   void deveUsarODiretorioDoUsuarioPorPadrao() {
      assertEquals(Path.of("casa",".mcp-academico","academico.db"),
                   DatabaseLocation.resolve(Map.of(),"casa"));
   }

   @Test
   void deveIgnorarVariavelEmBranco() {
      assertEquals(Path.of("casa",".mcp-academico","academico.db"),
                   DatabaseLocation.resolve(
                         Map.of(DatabaseLocation.VARIABLE,"  "),"casa"));
   }

   @Test
   void deveUsarOCaminhoDaVariavelDeAmbiente() {
      Path resolved = DatabaseLocation.resolve(
                         Map.of(DatabaseLocation.VARIABLE,"dados/banco.db"),
                         "casa");

      assertTrue(resolved.isAbsolute());
      assertTrue(resolved.endsWith(Path.of("dados","banco.db")));
   }
}
