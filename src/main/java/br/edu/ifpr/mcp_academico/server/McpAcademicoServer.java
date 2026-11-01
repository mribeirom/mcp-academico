// pacote (pasta) do servidor MCP
package br.edu.ifpr.mcp_academico.server;

// importa as classes que escrevem mensagens de log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// importa os contratos das ferramentas, resources e prompts
import br.edu.ifpr.mcp_academico.server.support.McpPromptDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpResourceDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpResourceTemplateDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpToolDefinition;
// importa as classes da biblioteca MCP (SDK) usadas para criar o servidor
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
// importa a classe que converte objetos para JSON e JSON para objetos
import tools.jackson.databind.json.JsonMapper;

// classe principal do servidor MCP: é ela que o comando "java -jar" executa
public class McpAcademicoServer {
   // objeto que escreve mensagens de log (no terminal de erros, não no STDOUT)
   private static final Logger LOG =
                              LoggerFactory.getLogger(McpAcademicoServer.class);

   // nome do servidor, mostrado pelos clientes MCP
   private static final String NAME    = "mcp-academico";
   // versão do servidor
   private static final String VERSION = "1.0.0";

   // monta a configuração básica do servidor
   private SyncSpecification<SingleSessionSyncSpecification> configMcpServer() {
      // cria o objeto para comunicação entre servidor e cliente
      JacksonMcpJsonMapper         jsonMapper        =
                                     new JacksonMcpJsonMapper(new JsonMapper());
      // estabelece que a comunicação usará o transporte STDIO
      StdioServerTransportProvider transportProvider =
                                   new StdioServerTransportProvider(jsonMapper);
      // identifica quais "capacidades" o servidor disponibiliza
      //
      // OBSERVAÇÃO:
      // o valor 'false' indica somente que as listas são fixas durante a
      // execução, ou seja, o servidor não notifica alterações nas mesmas
      ServerCapabilities capabilities    = ServerCapabilities.builder()
                                                             .tools(false)
                                                             .resources(false,
                                                                        false)
                                                             .prompts(false)
                                                             .logging()
                                                             .build();
      // identificação do servidor (nome e versão)
      Implementation     implementation  = Implementation.builder(NAME,VERSION)
                                                         .build();
      // configura o servidor com transporte STDIO
      return McpServer.sync(transportProvider)
                      .serverInfo(implementation)
                      .capabilities(capabilities);
   }

   // registra no servidor as ferramentas, os resources e os prompts
   private void register(SyncSpecification<SingleSessionSyncSpecification> mcp,
                         ServerFeatures features) {
      // pega cada ferramenta, transforma no formato da biblioteca MCP e
      // registra a lista no servidor
      mcp.tools(features.tools()
                        .stream()
                        .map(McpToolDefinition::specification)
                        .toList());
      // faz o mesmo com os resources (dados de leitura com endereço fixo)
      mcp.resources(features.resources()
                            .stream()
                            .map(McpResourceDefinition::specification)
                            .toList());
      // faz o mesmo com os templates (resources com partes variáveis no
      // endereço, como o ID do aluno)
      mcp.resourceTemplates(features.resourceTemplates()
                                    .stream()
                                    .map(McpResourceTemplateDefinition::specification)
                                    .toList());
      // faz o mesmo com os prompts (modelos de pergunta para a IA)
      mcp.prompts(features.prompts()
                          .stream()
                          .map(McpPromptDefinition::specification)
                          .toList());
   }

   // cria e inicia o servidor com as funcionalidades recebidas
   public McpAcademicoServer(ServerFeatures features) {
      // configura/inicializa o servidor
      SyncSpecification<SingleSessionSyncSpecification> mcp = configMcpServer();

      // registra o que está disponível neste servidor
      register(mcp,features);
      // cria o servidor, que encerra sozinho, com código de saída 0, quando o
      // cliente fecha a entrada padrão (STDIN)
      mcp.build();

      // escreve no log que o servidor foi iniciado
      LOG.info("MCP Acadêmico iniciado via STDIO...");
   }

   // ponto de partida do programa
   public static void main(String[] args) {
      // monta todas as peças da aplicação e inicia o servidor
      new McpAcademicoServer(ServerFeatures.assemble());
   }
}
