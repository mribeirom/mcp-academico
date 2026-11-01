package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.server.prompts.AnalisarDesempenhoPrompt;
import br.edu.ifpr.mcp_academico.server.prompts.PlanoRecuperacaoPrompt;
import br.edu.ifpr.mcp_academico.server.prompts.RelatorioTurmaPrompt;
import br.edu.ifpr.mcp_academico.server.resources.DisciplinasResource;
import br.edu.ifpr.mcp_academico.server.resources.HistoricoAlunoResource;
import br.edu.ifpr.mcp_academico.server.resources.RegrasAprovacaoResource;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

// simula a aplicação cliente usando os resources e prompts do servidor real
class McpAcademicoServerIT extends McpServerITSupport {
   private static String read(String uri) {
      return ((TextResourceContents) client.readResource(
                        ReadResourceRequest.builder(uri).build())
                                           .contents().get(0)).text();
   }

   private static GetPromptRequest prompt(String name,
                                          Map<String, Object> arguments) {
      return GetPromptRequest.builder(name).arguments(arguments).build();
   }

   private static String textOf(GetPromptRequest request) {
      return ((TextContent) client.getPrompt(request).messages().get(0)
                                  .content()).text();
   }

   @Test
   void deveAnunciarSuasCapacidades() {
      ServerCapabilities capabilities = client.getServerCapabilities();

      assertTrue(capabilities.tools() != null);
      assertTrue(capabilities.resources() != null);
      assertTrue(capabilities.prompts() != null);
      assertTrue(capabilities.logging() != null);
   }

   @Test
   void deveDisponibilizarResources() {
      assertEquals(List.of(DisciplinasResource.URI,RegrasAprovacaoResource.URI),
                   client.listResources().resources().stream()
                         .map(resource -> resource.uri()).sorted().toList());
      assertEquals(List.of(HistoricoAlunoResource.URI_TEMPLATE),
                   client.listResourceTemplates().resourceTemplates().stream()
                         .map(template -> template.uriTemplate()).toList());
   }

   @Test
   void deveLerOCatalogoDeDisciplinas() {
      String text = read(DisciplinasResource.URI);

      assertTrue(text.contains("Programação Orientada a Objetos"),text);
   }

   @Test
   void deveLerRegrasDeAprovacaoComAcentos() {
      String text = read(RegrasAprovacaoResource.URI);

      assertTrue(text.contains("Critérios de aprovação"),text);
      assertTrue(text.contains("6,0"),text);
   }

   @Test
   void deveLerHistoricoPeloTemplate() {
      String text = read("academico://alunos/1/historico");

      assertTrue(text.contains("Ana Luíza Gonçalves"),text);
      assertTrue(text.contains("APROVADO"),text);
   }

   @Test
   void deveFalharAoLerHistoricoComIdInvalido() {
      McpError error = assertThrows(McpError.class,
                             () -> read("academico://alunos/abc/historico"));

      assertTrue(error.getMessage().contains("ARGUMENTO_INVALIDO"),
                 error.getMessage());
   }

   @Test
   void deveFalharAoLerHistoricoDeAlunoInexistente() {
      McpError error = assertThrows(McpError.class,
                             () -> read("academico://alunos/999/historico"));

      assertTrue(error.getMessage().contains("ALUNO_NAO_ENCONTRADO"),
                 error.getMessage());
   }

   @Test
   void deveFalharAoLerResourceInexistente() {
      assertThrows(McpError.class,() -> read("academico://nada"));
   }

   @Test
   void deveDisponibilizarPrompts() {
      assertEquals(List.of(AnalisarDesempenhoPrompt.NAME,
                           PlanoRecuperacaoPrompt.NAME,
                           RelatorioTurmaPrompt.NAME),
                   client.listPrompts().prompts().stream()
                         .map(prompt -> prompt.name()).sorted().toList());
   }

   @Test
   void deveObterAnaliseDeDesempenhoComOBoletim() {
      String text = textOf(prompt(AnalisarDesempenhoPrompt.NAME,
                                  Map.of("aluno_id","1","semestre","2026/1")));

      assertTrue(text.contains("Ana Luíza Gonçalves"),text);
   }

   @Test
   void deveObterPlanoDeRecuperacaoComASituacao() {
      String text = textOf(prompt(PlanoRecuperacaoPrompt.NAME,
                                  Map.of("aluno_id","3","disciplina_id","1",
                                         "semestre","2026/1")));

      assertTrue(text.contains("REPROVADO_NOTA"),text);
   }

   @Test
   void deveObterRelatorioDaTurmaComEstatisticas() {
      String text = textOf(prompt(RelatorioTurmaPrompt.NAME,
                                  Map.of("disciplina_id","1",
                                         "semestre","2026/1")));

      assertTrue(text.contains("taxa_aprovacao"),text);
      assertTrue(text.contains("Luís Fernando Müller"),text);
   }

   @Test
   void deveFalharAoObterPromptSemArgumentoObrigatorio() {
      McpError error = assertThrows(McpError.class,
             () -> client.getPrompt(prompt(AnalisarDesempenhoPrompt.NAME,
                                           Map.of("aluno_id","1"))));

      assertTrue(error.getMessage().contains("semestre"),error.getMessage());
   }

   @Test
   void deveFalharAoObterPromptInexistente() {
      assertThrows(McpError.class,
                   () -> client.getPrompt(prompt("nada",Map.of())));
   }
}
