// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa as classes da biblioteca MCP para respostas de resources
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

// cria as respostas à leitura de resources
public final class ResourceResults {
   // construtor privado: esta classe não deve ser criada com "new"
   private ResourceResults() {}

   // cria uma resposta de texto: o endereço do resource, o tipo do conteúdo
   // (como "application/json" ou "text/markdown") e o próprio texto
   public static ReadResourceResult text(String uri, String mimeType,
                                         String text) {
      // monta o conteúdo com o endereço, o texto e o tipo
      TextResourceContents contents = TextResourceContents.builder(uri,text)
                                                          .mimeType(mimeType)
                                                          .build();

      // coloca o conteúdo na resposta
      return ReadResourceResult.builder(List.of(contents)).build();
   }
}
