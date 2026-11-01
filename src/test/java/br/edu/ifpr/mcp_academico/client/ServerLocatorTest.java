package br.edu.ifpr.mcp_academico.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerLocatorTest {
   private static final String JAR = "mcp_academico_server.jar";

   @TempDir
   Path directory;

   private Path createJar(Path parent) throws IOException {
      Files.createDirectories(parent);

      return Files.createFile(parent.resolve(JAR));
   }

   @Test
   void deveLocalizarOServidorAPartirDoDiretorioDoCliente() throws IOException {
      Path jar = createJar(directory.resolve("server"));
      Path dir = directory.resolve("target").resolve("dist-client");

      assertEquals(jar.toFile().getCanonicalFile(),
                   ServerLocator.locate(Map.of(),dir).getCanonicalFile());
   }

   @Test
   void deveDarPreferenciaAoCaminhoDaVariavelDeAmbiente() throws IOException {
      Path jar = createJar(directory.resolve("outro"));

      assertEquals(jar.toFile(),
                   ServerLocator.locate(Map.of(ServerLocator.VARIABLE,
                                               jar.toString()),directory));
   }

   @Test
   void deveIgnorarVariavelEmBranco() throws IOException {
      Path jar = createJar(directory.resolve("server"));
      Path dir = directory.resolve("target").resolve("dist-client");

      assertEquals(jar.toFile().getCanonicalFile(),
                   ServerLocator.locate(Map.of(ServerLocator.VARIABLE," "),dir)
                                .getCanonicalFile());
   }

   @Test
   void deveExplicarQueOServidorNaoFoiEncontrado() {
      IllegalStateException ex = assertThrows(IllegalStateException.class,
                       () -> ServerLocator.locate(Map.of(),directory));

      assertTrue(ex.getMessage().contains(ServerLocator.VARIABLE));
   }
}
