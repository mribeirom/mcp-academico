// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o formato de template de resource usado pela biblioteca MCP
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;

// contrato que todo template de resource cumpre: saber se transformar no
// formato da biblioteca MCP
public interface McpResourceTemplateDefinition {
   // devolve o template pronto para ser registrado no servidor
   public SyncResourceTemplateSpecification specification();
}
