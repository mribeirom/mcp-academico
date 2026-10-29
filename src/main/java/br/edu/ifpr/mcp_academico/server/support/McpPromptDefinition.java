// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o formato de prompt usado pela biblioteca MCP
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;

// contrato que todo prompt cumpre: saber se transformar no formato da
// biblioteca MCP
public interface McpPromptDefinition {
   // devolve o prompt pronto para ser registrado no servidor
   public SyncPromptSpecification specification();
}
