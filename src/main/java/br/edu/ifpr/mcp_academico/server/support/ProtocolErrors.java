// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa Supplier, que representa um "pedaço de código" que devolve um valor
import java.util.function.Supplier;

// importa as classes que escrevem mensagens de log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// importa o erro da aplicação e os códigos de erro
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
// importa o erro do protocolo MCP e os seus códigos
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;

// resources e prompts não possuem o indicador 'isError' das ferramentas e, por
// isso, seus erros são informados ao cliente como erros do protocolo
public final class ProtocolErrors {
   // objeto que escreve mensagens de log
   private static final Logger LOG =
                                  LoggerFactory.getLogger(ProtocolErrors.class);

   // construtor privado: esta classe não deve ser criada com "new"
   private ProtocolErrors() {}

   // executa um código e troca qualquer erro por um erro do protocolo MCP
   public static <T> T guard(Supplier<T> action) {
      // executa o código e devolve o resultado
      try { return action.get(); }
      // erro previsto: o cliente informou algo errado (parâmetro inválido)
      catch (DomainException ex) {
         throw error(ErrorCodes.INVALID_PARAMS,ex.code(),ex.getMessage());
      }
      // erro inesperado
      catch (RuntimeException ex) {
         // sem esta captura, o SDK enviaria ao cliente a mensagem da exceção
         LOG.error("Erro inesperado no servidor MCP",ex);
         // envia só uma mensagem genérica de erro interno
         throw error(ErrorCodes.INTERNAL_ERROR,ErrorCode.ERRO_INTERNO,
                     "Erro interno no servidor.");
      }
   }

   // monta o erro do protocolo com o código e a mensagem
   private static McpError error(int protocolCode, ErrorCode code,
                                 String message) {
      return McpError.builder(protocolCode)
                     .message(code.format(message))
                     .build();
   }
}
