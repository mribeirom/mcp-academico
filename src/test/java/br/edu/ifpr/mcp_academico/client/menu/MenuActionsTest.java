package br.edu.ifpr.mcp_academico.client.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.client.FakeMcpGateway;
import br.edu.ifpr.mcp_academico.client.ResultPrinter;
import br.edu.ifpr.mcp_academico.client.ScriptedConsole;
import br.edu.ifpr.mcp_academico.client.arguments.ArgumentPrompter;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.Tool;

class MenuActionsTest {
   private final FakeMcpGateway gateway = new FakeMcpGateway();

   private ToolsAction tools(ScriptedConsole scripted) {
      return new ToolsAction(gateway,scripted.console(),
                             new ArgumentPrompter(scripted.console()),
                             new ResultPrinter(scripted.console()));
   }

   private ResourcesAction resources(ScriptedConsole scripted) {
      return new ResourcesAction(gateway,scripted.console(),
                                 new ResultPrinter(scripted.console()));
   }

   private PromptsAction prompts(ScriptedConsole scripted) {
      return new PromptsAction(gateway,scripted.console(),
                               new ArgumentPrompter(scripted.console()),
                               new ResultPrinter(scripted.console()));
   }

   @Test
   void deveExecutarQualquerFerramentaAPartirDoEsquema() {
      gateway.tools.add(Tool.builder("somar",
              Map.of("type","object",
                     "properties",Map.of("itens",
                                  Map.of("type","array",
                                         "items",Map.of("type","number"))),
                     "required",List.of("itens")))
                            .description("Soma valores").build());
      ScriptedConsole scripted = new ScriptedConsole("somar","1,5");

      tools(scripted).execute();

      assertEquals("somar",gateway.lastName);
      assertEquals(Map.of("itens",List.of(1.0,5.0)),gateway.lastArguments);
      assertTrue(scripted.output().contains("Soma valores"));
      assertTrue(scripted.output().contains("executada"));
   }

   @Test
   void naoDeveExecutarFerramentaAoVoltar() {
      gateway.tools.add(Tool.builder("somar",Map.of("type","object")).build());

      tools(new ScriptedConsole("")).execute();

      assertNull(gateway.lastName);
   }

   @Test
   void deveListarResourcesETemplatesJuntos() {
      gateway.resources.add(Resource.builder("a://regras","regras")
                                    .description("Regras").build());
      gateway.templates.add(ResourceTemplate.builder("a://alunos/{id}","aluno")
                                            .description("Aluno").build());
      ScriptedConsole scripted = new ScriptedConsole("1");

      resources(scripted).execute();

      assertEquals("a://regras",gateway.lastUri);
      assertTrue(scripted.output().contains("a://alunos/{id}"));
      assertTrue(scripted.output().contains("conteúdo"));
   }

   @Test
   void deveSolicitarAsVariaveisDeUmTemplate() {
      gateway.templates.add(ResourceTemplate.builder("a://alunos/{id}","aluno")
                                            .build());

      resources(new ScriptedConsole("1","7")).execute();

      assertEquals("a://alunos/7",gateway.lastUri);
   }

   @Test
   void naoDeveLerTemplateSeAEntradaTerminar() {
      gateway.templates.add(ResourceTemplate.builder("a://alunos/{id}","aluno")
                                            .build());

      resources(new ScriptedConsole("1")).execute();

      assertNull(gateway.lastUri);
   }

   @Test
   void deveObterPromptComArgumentosObrigatoriosEOpcionais() {
      gateway.prompts.add(Prompt.builder("analisar")
              .description("Análise")
              .arguments(List.of(
                   PromptArgument.builder("aluno_id").required(true).build(),
                   PromptArgument.builder("foco").required(false).build()))
              .build());
      ScriptedConsole scripted = new ScriptedConsole("analisar","7","");

      prompts(scripted).execute();

      assertEquals("analisar",gateway.lastName);
      assertEquals(Map.of("aluno_id","7"),gateway.lastArguments);
      assertTrue(scripted.output().contains("mensagem"));
   }

   @Test
   void deveInformarAsOpcoesDoMenu() {
      ScriptedConsole scripted = new ScriptedConsole();

      assertEquals("Ferramentas",tools(scripted).label());
      assertEquals("Resources",resources(scripted).label());
      assertEquals("Prompts",prompts(scripted).label());
   }
}
