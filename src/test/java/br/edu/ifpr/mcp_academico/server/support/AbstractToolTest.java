package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

class AbstractToolTest {
   // dublê de teste: ferramenta cuja tarefa é definida por cada teste
   private static class FakeTool extends AbstractTool {
      private final Function<ArgumentReader, CallToolResult> action;

      FakeTool(Function<ArgumentReader, CallToolResult> action) {
         this.action = action;
      }

      @Override
      protected String name() {
         return "ferramenta";
      }

      @Override
      protected String title() {
         return "Ferramenta";
      }

      @Override
      protected String description() {
         return "Descrição";
      }

      @Override
      protected Map<String, Object> inputSchema() {
         return JsonSchemaBuilder.object().build();
      }

      @Override
      protected ToolAnnotations annotations() {
         return DESTRUCTIVE;
      }

      @Override
      protected CallToolResult execute(ArgumentReader arguments) {
         return action.apply(arguments);
      }
   }

   private static String textOf(CallToolResult result) {
      return ((TextContent) result.content().get(0)).text();
   }

   @Test
   void deveMontarAEspecificacaoComOsDadosDaFerramenta() {
      SyncToolSpecification specification =
                         new FakeTool(arguments -> null).specification();
      Tool                  tool          = specification.tool();

      assertEquals("ferramenta",tool.name());
      assertEquals("Ferramenta",tool.title());
      assertEquals("Descrição",tool.description());
      assertTrue(tool.annotations().destructiveHint());
      assertNotNull(tool.inputSchema());
      assertNotNull(specification.callHandler());
   }

   @Test
   void deveRetornarOResultadoDaTarefa() {
      CallToolResult expected = ToolResults.json(Map.of("ok",true));

      assertSame(expected,new FakeTool(arguments -> expected).call(Map.of()));
   }

   @Test
   void deveEntregarOsArgumentosParaATarefa() {
      CallToolResult result = new FakeTool(arguments -> ToolResults.structured(
                      arguments.requiredString("nome"),Map.of()))
                                                .call(Map.of("nome","Ana"));

      assertEquals("Ana",textOf(result));
   }

   @Test
   void deveConverterErroDeDominioEmResultadoDeErro() {
      CallToolResult result = new FakeTool(arguments -> {
         throw new DomainException(ErrorCode.ARGUMENTO_INVALIDO,"inválido");
      }).call(Map.of());

      assertTrue(result.isError());
      assertEquals("[ARGUMENTO_INVALIDO] inválido",textOf(result));
   }

   @Test
   void deveOcultarDetalhesDeErroInesperado() {
      CallToolResult result = new FakeTool(arguments -> {
         throw new IllegalStateException("senha do banco");
      }).call(Map.of());

      assertTrue(result.isError());
      assertTrue(textOf(result).startsWith("[ERRO_INTERNO]"));
      assertFalse(textOf(result).contains("senha do banco"));
   }
}
