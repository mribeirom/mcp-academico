package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_academico.persistence.jdbc.DatabaseLocation;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import tools.jackson.databind.json.JsonMapper;

// executa o servidor MCP real e conecta um cliente a ele, usando um banco de
// dados temporário para que os testes não alterem os dados do usuário
abstract class McpServerITSupport {
   private static final Logger LOG =
                              LoggerFactory.getLogger(McpServerITSupport.class);
   // nome e caminho para o servidor MCP
   private static final String SERVER_PATH = "server/mcp_academico_server.jar";

   protected static McpSyncClient client;

   private static Path directory;

   static ServerParameters serverParameters(Path databaseFile) {
      File serverJar = new File(SERVER_PATH);
      assertTrue(serverJar.isFile(),"JAR do servidor não encontrado: " +
                                    serverJar.getAbsolutePath());
      // o servidor é executado pela mesma JVM dos testes, pois a versão do
      // comando "java" disponível no PATH pode ser outra
      String java = Path.of(System.getProperty("java.home"),"bin","java")
                        .toString();

      return ServerParameters.builder(java)
                             .args("-jar",serverJar.getAbsolutePath())
                             .env(Map.of(DatabaseLocation.VARIABLE,
                                         databaseFile.toString()))
                             .build();
   }

   static McpSyncClient connect(Path databaseFile) {
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
                                                              new JsonMapper());
      StdioClientTransport transport  = new StdioClientTransport(
                                                serverParameters(databaseFile),
                                                jsonMapper);

      transport.setStdErrorHandler(line -> LOG.info("[SERVER STDERR] " + line));

      McpSyncClient connected = McpClient.sync(transport).build();

      connected.initialize();

      return connected;
   }

   @BeforeAll
   static void setUpBeforeClass() throws Exception {
      // antes que a classe de testes seja carregada, este método providencia
      // alguma inicialização ou alocação de recursos, neste caso, o servidor
      // MCP é executado e o cliente se conecta a ele
      directory = Files.createTempDirectory("mcp-academico-it");
      client    = connect(directory.resolve("academico.db"));
   }

   @AfterAll
   static void tearDownAfterClass() throws Exception {
      // ao terminar todos os testes, todos os métodos nesta classe foram
      // executados, o cliente se desconecta do servidor
      if (client != null)
         client.closeGracefully();
   }
}
