// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa o tipo List (lista) e o Map (mapa que liga uma chave a um valor)
import java.util.List;
import java.util.Map;

// importa as classes da biblioteca MCP que representam as respostas
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.Tool;

// operações que o cliente solicita ao servidor MCP: o menu depende desta
// abstração, e não diretamente do SDK
public interface McpGateway {
   // pede a lista de ferramentas
   public List<Tool> listTools();
   // executa uma ferramenta com os argumentos informados
   public CallToolResult callTool(String name, Map<String, Object> arguments);
   // pede a lista de resources
   public List<Resource> listResources();
   // pede a lista de templates de resources
   public List<ResourceTemplate> listResourceTemplates();
   // lê o conteúdo de um resource pelo endereço
   public ReadResourceResult readResource(String uri);
   // pede a lista de prompts
   public List<Prompt> listPrompts();
   // gera o texto de um prompt com os argumentos informados
   public GetPromptResult getPrompt(String name, Map<String, Object> arguments);
}
