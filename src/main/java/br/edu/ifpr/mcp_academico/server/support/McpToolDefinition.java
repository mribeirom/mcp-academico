// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o formato de ferramenta usado pela biblioteca MCP
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

// contrato que toda ferramenta cumpre: saber se transformar no formato da
// biblioteca MCP
public interface McpToolDefinition {
   // devolve a ferramenta pronta para ser registrada no servidor
   public SyncToolSpecification specification();
}
