// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa as classes que escrevem mensagens de log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// importa o erro da aplicação e os códigos de erro
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
// importa as classes da biblioteca MCP usadas para criar ferramentas
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// padrão de projeto Template Method: define os passos comuns a todas as
// ferramentas, deixando para as subclasses somente a sua descrição e a tarefa
// a ser realizada
public abstract class AbstractTool implements McpToolDefinition {
   // objeto que escreve mensagens de log
   private static final Logger LOG =
                                    LoggerFactory.getLogger(AbstractTool.class);

   // indicações para os assistentes de IA sobre os efeitos de cada ferramenta
   // READ_ONLY: só lê dados; pode ser repetida sem mudar nada
   protected static final ToolAnnotations READ_ONLY   =
                                 ToolAnnotations.builder()
                                                .readOnlyHint(true)
                                                .destructiveHint(false)
                                                .idempotentHint(true)
                                                .openWorldHint(false)
                                                .build();
   // WRITE: grava dados novos, sem apagar nada
   protected static final ToolAnnotations WRITE       =
                                 ToolAnnotations.builder()
                                                .readOnlyHint(false)
                                                .destructiveHint(false)
                                                .idempotentHint(false)
                                                .openWorldHint(false)
                                                .build();
   // DESTRUCTIVE: altera ou desfaz algo que já existia, como cancelar
   protected static final ToolAnnotations DESTRUCTIVE =
                                 ToolAnnotations.builder()
                                                .readOnlyHint(false)
                                                .destructiveHint(true)
                                                .idempotentHint(false)
                                                .openWorldHint(false)
                                                .build();

   // cada ferramenta informa o seu nome técnico, como "gerar_boletim"
   protected abstract String name();
   // cada ferramenta informa o seu título, mostrado ao usuário
   protected abstract String title();
   // cada ferramenta informa a sua descrição, lida pela IA
   protected abstract String description();
   // cada ferramenta informa os seus parâmetros (esquema JSON)
   protected abstract Map<String, Object> inputSchema();
   // cada ferramenta informa se lê, grava ou altera dados
   protected abstract ToolAnnotations annotations();
   // cada ferramenta faz a sua tarefa e devolve o resultado
   protected abstract CallToolResult execute(ArgumentReader arguments);

   // transforma esta ferramenta no formato que a biblioteca MCP registra
   @Override
   public final SyncToolSpecification specification() {
      // define a ferramenta
      Tool tool = Tool.builder(name(),inputSchema())
                      .title(title())
                      .description(description())
                      .annotations(annotations())
                      .build();
      // vincula a definição (acima) à implementação da ferramenta
      return SyncToolSpecification.builder()
                                  .tool(tool)
                                  .callHandler(this::handle)
                                  .build();
   }

   // executa a ferramenta garantindo que qualquer erro chegue ao cliente como
   // um resultado, com 'isError' verdadeiro
   public final CallToolResult call(Map<String, Object> arguments) {
      // executa a tarefa da subclasse com os argumentos recebidos
      try { return execute(new ArgumentReader(arguments)); }
      // erro previsto (como aluno não encontrado): devolve a mensagem dele
      catch (DomainException ex) {
         return ToolResults.failure(ex.code(),ex.getMessage());
      }
      // erro inesperado
      catch (RuntimeException ex) {
         // detalhes internos ficam no log e não são enviados ao cliente
         LOG.error("Erro inesperado na ferramenta '{}'",name(),ex);

         // devolve uma mensagem genérica de erro interno
         return ToolResults.failure(ErrorCode.ERRO_INTERNO,
                                    "Erro interno ao executar a ferramenta.");
      }
   }

   // método chamado pela biblioteca MCP quando o cliente usa a ferramenta
   private CallToolResult handle(McpSyncServerExchange exchange,
                                 CallToolRequest request) {
      // executa a ferramenta com os argumentos enviados pelo cliente
      CallToolResult result = call(request.arguments());
      // envia uma notificação ao cliente
      exchange.loggingNotification(
              LoggingMessageNotification.builder(LoggingLevel.INFO,
                                                 "Ferramenta executada: " +
                                                 name())
                                        .logger(getClass().getSimpleName())
                                        .build());
      // devolve o resultado ao cliente
      return result;
   }
}
