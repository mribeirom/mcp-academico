// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o formato de resource usado pela biblioteca MCP
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

// contrato que todo resource cumpre: saber se transformar no formato da
// biblioteca MCP
public interface McpResourceDefinition {
   // devolve o resource pronto para ser registrado no servidor
   public SyncResourceSpecification specification();
}
