package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class ToolResultsTest {
   private static String textOf(CallToolResult result) {
      return ((TextContent) result.content().get(0)).text();
   }

   @Test
   void deveCriarResultadoEmJson() {
      CallToolResult result = ToolResults.json(Map.of("id",1));

      assertFalse(result.isError());
      assertTrue(textOf(result).contains("\"id\" : 1"),textOf(result));
      assertEquals(Map.of("id",1),result.structuredContent());
   }

   @Test
   void deveCriarResultadoComTextoPersonalizado() {
      CallToolResult result = ToolResults.structured("ok",Map.of("id",1));

      assertEquals("ok",textOf(result));
      assertFalse(result.isError());
      assertEquals(Map.of("id",1),result.structuredContent());
   }

   @Test
   void deveCriarResultadoDeErroComCodigoEMensagem() {
      CallToolResult result = ToolResults.failure(ErrorCode.ARGUMENTO_INVALIDO,
                                                  "valor inválido");

      assertTrue(result.isError());
      assertEquals("[ARGUMENTO_INVALIDO] valor inválido",textOf(result));
      assertEquals(Map.of("codigo","ARGUMENTO_INVALIDO",
                          "mensagem","valor inválido"),
                   result.structuredContent());
   }
}
