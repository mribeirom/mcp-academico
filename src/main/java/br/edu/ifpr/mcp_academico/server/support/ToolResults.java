// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa os códigos de erro e o resultado de ferramenta da biblioteca MCP
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

// cria os resultados retornados pelas ferramentas ao cliente MCP
public final class ToolResults {
   // construtor privado: esta classe não deve ser criada com "new"
   private ToolResults() {}

   // cria um resultado de sucesso a partir de um objeto. O conteúdo textual
   // atende clientes que não leem o conteúdo estruturado
   public static CallToolResult json(Object data) {
      // envia o objeto em duas formas: texto JSON e dados estruturados
      return structured(JsonSupport.toJson(data),JsonSupport.toMap(data));
   }

   // cria um resultado de sucesso com um texto e os dados estruturados
   public static CallToolResult structured(String text,
                                           Map<String, Object> data) {
      return CallToolResult.builder()
                           .addTextContent(text)
                           .structuredContent(data)
                           .isError(false)
                           .build();
   }

   // um erro retornado como resultado, e não como exceção, chega à IA como
   // conteúdo da ferramenta, permitindo que ela corrija a chamada
   public static CallToolResult failure(ErrorCode code, String message) {
      // texto com o código na frente, dados com o código e a mensagem, e o
      // indicador de erro ligado
      return CallToolResult.builder()
                           .addTextContent(code.format(message))
                           .structuredContent(Map.of("codigo",code.name(),
                                                     "mensagem",message))
                           .isError(true)
                           .build();
   }
}
