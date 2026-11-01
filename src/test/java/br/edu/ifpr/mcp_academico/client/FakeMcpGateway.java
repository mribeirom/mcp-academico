package br.edu.ifpr.mcp_academico.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;

// dublê de teste: servidor MCP em memória que registra as chamadas recebidas
public class FakeMcpGateway implements McpGateway {
   public final List<Tool>             tools     = new ArrayList<>();
   public final List<Resource>         resources = new ArrayList<>();
   public final List<ResourceTemplate> templates = new ArrayList<>();
   public final List<Prompt>           prompts   = new ArrayList<>();

   public String              lastName;
   public Map<String, Object> lastArguments;
   public String              lastUri;

   @Override
   public List<Tool> listTools() {
      return tools;
   }

   @Override
   public CallToolResult callTool(String name, Map<String, Object> arguments) {
      lastName      = name;
      lastArguments = arguments;

      return CallToolResult.builder().addTextContent("executada").build();
   }

   @Override
   public List<Resource> listResources() {
      return resources;
   }

   @Override
   public List<ResourceTemplate> listResourceTemplates() {
      return templates;
   }

   @Override
   public ReadResourceResult readResource(String uri) {
      lastUri = uri;

      return ReadResourceResult.builder(List.of(
                  TextResourceContents.builder(uri,"conteúdo").build())).build();
   }

   @Override
   public List<Prompt> listPrompts() {
      return prompts;
   }

   @Override
   public GetPromptResult getPrompt(String name,
                                    Map<String, Object> arguments) {
      lastName      = name;
      lastArguments = arguments;

      return GetPromptResult.builder(List.of(PromptMessage.builder(
                  Role.USER,TextContent.builder("mensagem").build()).build()))
                            .build();
   }
}
