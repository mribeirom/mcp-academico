package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

// reproduz o Claude Desktop aberto enquanto o cliente é executado: dois
// processos do servidor usando o mesmo arquivo do banco de dados
class ConcorrenciaIT {
   private static final int ALUNOS_DE_EXEMPLO    = 8;
   private static final int CADASTROS_POR_CLIENTE = 25;

   private static CallToolResult cadastrar(McpSyncClient client, String ra) {
      return client.callTool(CallToolRequest.builder("cadastrar_aluno")
             .arguments(Map.of("nome","Aluno " + ra,
                               "email",ra + "@example.com","ra",ra))
             .build());
   }

   private static void cadastrarVarios(McpSyncClient client, String prefixo) {
      for (int i = 0; i < CADASTROS_POR_CLIENTE; i++) {
         CallToolResult result = cadastrar(client,prefixo + i);

         assertFalse(result.isError(),String.valueOf(result.content()));
      }
   }

   @Test
   void doisServidoresDevemCompartilharOMesmoBanco() throws Exception {
      Path            file     = Files.createTempDirectory("mcp-concorrencia")
                                      .resolve("academico.db");
      ExecutorService executor = Executors.newFixedThreadPool(2);
      List<McpSyncClient> clients = new ArrayList<>();

      try {
         // os dois servidores criam o banco de dados ao mesmo tempo
         Future<McpSyncClient> primeiro = executor.submit(
                                 () -> McpServerITSupport.connect(file));
         Future<McpSyncClient> segundo  = executor.submit(
                                 () -> McpServerITSupport.connect(file));

         clients.add(primeiro.get());
         clients.add(segundo.get());

         // os dois servidores gravam ao mesmo tempo
         Future<?> gravacoesA = executor.submit(
                     () -> cadastrarVarios(clients.get(0),"A"));
         Future<?> gravacoesB = executor.submit(
                     () -> cadastrarVarios(clients.get(1),"B"));

         gravacoesA.get();
         gravacoesB.get();

         Object alunos = clients.get(0).callTool(
                  CallToolRequest.builder("buscar_alunos")
                                 .arguments(Map.of("limite",100))
                                 .build()).structuredContent();

         assertEquals(ALUNOS_DE_EXEMPLO + 2 * CADASTROS_POR_CLIENTE,
                      ((List<?>) ((Map<?, ?>) alunos).get("alunos")).size());
      }
      finally {
         executor.shutdownNow();
         clients.forEach(McpSyncClient::closeGracefully);
      }
   }
}
