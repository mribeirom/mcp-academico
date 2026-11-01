package br.edu.ifpr.mcp_academico.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.BlobResourceContents;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ImageContent;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class ResultPrinterTest {
   private final ScriptedConsole scripted = new ScriptedConsole();
   private final ResultPrinter   printer  =
                                      new ResultPrinter(scripted.console());

   @Test
   void deveExibirOResultadoDeSucesso() {
      printer.print(CallToolResult.builder().addTextContent("ok")
                                  .isError(false).build());

      assertTrue(scripted.output().contains("Erro: Não"));
      assertTrue(scripted.output().contains("ok"));
   }

   @Test
   void deveExibirOResultadoDeErro() {
      printer.print(CallToolResult.builder().addTextContent("falhou")
                                  .isError(true).build());

      assertTrue(scripted.output().contains("Erro: Sim"));
   }

   @Test
   void deveExibirConteudoEstruturadoQuandoNaoHaTexto() {
      printer.print(CallToolResult.builder().content(List.of())
                                  .structuredContent(Map.of("id",1)).build());

      assertTrue(scripted.output().contains("{id=1}"));
   }

   @Test
   void deveExibirConteudoNaoTextualPelaDescricao() {
      printer.print(CallToolResult.builder()
                       .addContent(ImageContent.builder("AAAA","image/png")
                                               .build())
                       .build());

      assertTrue(scripted.output().contains("image/png"));
   }

   @Test
   void deveExibirUmResource() {
      printer.print(ReadResourceResult.builder(List.of(
              TextResourceContents.builder("a://b","Critérios").build()))
                                      .build());

      assertTrue(scripted.output().contains("Critérios"));
   }

   @Test
   void deveExibirResourceBinarioPelaDescricao() {
      printer.print(ReadResourceResult.builder(List.of(
              BlobResourceContents.builder("a://b","AAAA").build())).build());

      assertTrue(scripted.output().contains("a://b"));
   }

   @Test
   void deveExibirAsMensagensDeUmPrompt() {
      printer.print(GetPromptResult.builder(List.of(PromptMessage.builder(
              Role.USER,TextContent.builder("Analise").build()).build()))
                                   .build());

      assertTrue(scripted.output().contains("[USER] Analise"));
   }
}
