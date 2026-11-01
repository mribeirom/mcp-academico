package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.edu.ifpr.mcp_academico.server.support.McpPromptDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpToolDefinition;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Tool;

// verifica a composição do servidor e a descrição de tudo que ele disponibiliza
class ServerFeaturesTest {
   @TempDir
   Path directory;

   private ServerFeatures features;

   @BeforeEach
   void montar() {
      features = ServerFeatures.assemble(directory.resolve("academico.db"));
   }

   private List<Tool> tools() {
      return features.tools().stream()
                     .map(McpToolDefinition::specification)
                     .map(specification -> specification.tool())
                     .toList();
   }

   private List<Prompt> prompts() {
      return features.prompts().stream()
                     .map(McpPromptDefinition::specification)
                     .map(specification -> specification.prompt())
                     .toList();
   }

   @Test
   void deveDisponibilizarTudoQueFoiPlanejado() {
      assertEquals(12,features.tools().size());
      assertEquals(2,features.resources().size());
      assertEquals(1,features.resourceTemplates().size());
      assertEquals(3,features.prompts().size());
   }

   @Test
   void cadaFerramentaDeveTerNomeUnicoEmSnakeCase() {
      List<String> nomes = tools().stream().map(Tool::name).toList();

      assertEquals(nomes.size(),nomes.stream().distinct().count());
      nomes.forEach(nome -> assertTrue(nome.matches("[a-z]+(_[a-z]+)*"),nome));
   }

   @Test
   void cadaFerramentaDeveSerDescritaParaOAssistenteDeIA() {
      tools().forEach(tool -> {
         assertFalse(tool.title().isBlank(),tool.name());
         assertFalse(tool.description().isBlank(),tool.name());
         assertNotNull(tool.annotations(),tool.name());
      });
   }

   @Test
   void cadaEsquemaDeveSerUmObjetoSemPropriedadesAdicionais() {
      tools().forEach(tool -> {
         assertEquals("object",tool.inputSchema().get("type"),tool.name());
         assertEquals(false,tool.inputSchema().get("additionalProperties"),
                      tool.name());
      });
   }

   @Test
   void somenteOCancelamentoDeveSerDestrutivo() {
      assertEquals(List.of("cancelar_matricula"),
                   tools().stream()
                          .filter(tool -> tool.annotations().destructiveHint())
                          .map(Tool::name)
                          .toList());
   }

   @Test
   void cadaPromptDeveSerDescritoParaOAssistenteDeIA() {
      prompts().forEach(prompt -> {
         assertFalse(prompt.title().isBlank(),prompt.name());
         assertFalse(prompt.description().isBlank(),prompt.name());
         assertFalse(prompt.arguments().isEmpty(),prompt.name());
      });
   }

   @Test
   void deveReaproveitarUmBancoExistente() {
      assertEquals(12,ServerFeatures.assemble(directory.resolve("academico.db"))
                                    .tools().size());
   }
}
