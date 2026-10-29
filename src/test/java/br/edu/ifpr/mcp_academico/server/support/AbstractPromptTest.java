package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class AbstractPromptTest {
   // dublê de teste: prompt que falha ao gerar o texto, se assim for definido
   private static class FakePrompt extends AbstractPrompt {
      private final RuntimeException failure;

      FakePrompt(RuntimeException failure) {
         this.failure = failure;
      }

      @Override
      protected String name() {
         return "prompt";
      }

      @Override
      protected String title() {
         return "Prompt";
      }

      @Override
      protected String description() {
         return "Descrição";
      }

      @Override
      protected List<PromptArgument> arguments() {
         return List.of(required("nome","Nome"));
      }

      @Override
      protected String render(ArgumentReader arguments) {
         if (failure != null)
            throw failure;

         return "Olá, " + arguments.requiredString("nome");
      }
   }

   @Test
   void deveMontarAEspecificacao() {
      Prompt prompt = new FakePrompt(null).specification().prompt();

      assertEquals("prompt",prompt.name());
      assertEquals("Prompt",prompt.title());
      assertEquals("Descrição",prompt.description());
      assertTrue(prompt.arguments().get(0).required());
   }

   @Test
   void deveGerarUmaMensagemDoUsuario() {
      GetPromptResult result = new FakePrompt(null).get(Map.of("nome","Ana"));

      assertEquals("Descrição",result.description());
      assertEquals("Olá, Ana",
                   ((TextContent) result.messages().get(0).content()).text());
   }

   @Test
   void deveOcultarDetalhesDeErroInesperado() {
      McpError error = assertThrows(McpError.class,
           () -> new FakePrompt(new IllegalStateException("senha do banco"))
                                                         .get(Map.of()));

      assertEquals(ErrorCodes.INTERNAL_ERROR,error.getJsonRpcError().code());
      assertFalse(error.getMessage().contains("senha do banco"));
   }
}
