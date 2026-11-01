// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa as classes do Java para arquivos, caminhos, listas e mapas
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// importa as classes que escrevem mensagens de log
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// importa as classes da biblioteca MCP usadas pelo cliente
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.Tool;
// importa a classe que converte objetos para JSON e JSON para objetos
import tools.jackson.databind.json.JsonMapper;

// conversa com o servidor MCP usando a biblioteca oficial (SDK). Ao terminar,
// a conexão é fechada (por isso implementa AutoCloseable)
public final class SdkMcpGateway implements McpGateway, AutoCloseable {
   // objeto que escreve mensagens de log
   private static final Logger LOG =
                                   LoggerFactory.getLogger(SdkMcpGateway.class);

   // cliente MCP da biblioteca, já conectado ao servidor
   private final McpSyncClient client;

   // construtor privado: use o método "connect" para criar o objeto
   private SdkMcpGateway(McpSyncClient client) {
      // guarda o cliente conectado
      this.client = client;
   }

   // executa o servidor MCP e conecta o cliente a ele
   public static SdkMcpGateway connect(File serverJar,
                                       Map<String, String> environment) {
      // cria o cliente que vai iniciar o servidor
      McpSyncClient client = createClient(serverJar,environment);

      // inicializa o cliente com o transporte STDIO para o servidor
      client.initialize();
      // define o nível mínimo de "logging" que o cliente quer receber do
      // servidor
      client.setLoggingLevel(LoggingLevel.INFO);

      // devolve o objeto pronto para uso
      return new SdkMcpGateway(client);
   }

   // monta o cliente MCP que inicia o servidor como outro programa
   private static McpSyncClient createClient(File serverJar,
                                             Map<String, String> environment) {
      // o servidor é executado pela mesma JVM do cliente, pois a versão do
      // comando "java" disponível no PATH pode ser outra
      String           java   = Path.of(System.getProperty("java.home"),"bin",
                                        "java").toString();
      // comando que inicia o servidor: java -jar <arquivo do servidor>, com
      // as mesmas variáveis de ambiente do cliente
      ServerParameters params = ServerParameters.builder(java)
                                                .args("-jar",
                                                      serverJar.getAbsolutePath())
                                                .env(environment)
                                                .build();
      // cria o objeto para comunicação entre cliente e servidor
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
                                                              new JsonMapper());
      // estabelece que a comunicação usará o transporte STDIO
      StdioClientTransport transport  = new StdioClientTransport(params,
                                                                 jsonMapper);
      // a saída de erros do servidor fica disponível somente em nível TRACE,
      // pois a saída do cliente é a interface com o usuário
      transport.setStdErrorHandler(line -> LOG.trace("[SERVIDOR] {}",line));

      // cria o cliente com o transporte configurado
      return McpClient.sync(transport)
                      // registra o tratador que captura as notificações
                      // enviadas pelo servidor
                      .loggingConsumer(SdkMcpGateway::onNotification)
                      .build();
   }

   // escreve no log cada notificação recebida do servidor
   private static void onNotification(LoggingMessageNotification notification) {
      LOG.info("[NOTIFICAÇÃO MCP] Origem: {} | Nível: {} | Mensagem: {}",
               notification.logger(),
               notification.level(),
               notification.data());
   }

   // pede a lista de ferramentas ao servidor
   @Override
   public List<Tool> listTools() {
      return client.listTools().tools();
   }

   // pede ao servidor para executar uma ferramenta
   @Override
   public CallToolResult callTool(String name, Map<String, Object> arguments) {
      // monta o pedido com o nome da ferramenta e os argumentos
      return client.callTool(CallToolRequest.builder(name)
                                            .arguments(arguments)
                                            .build());
   }

   // pede a lista de resources ao servidor
   @Override
   public List<Resource> listResources() {
      return client.listResources().resources();
   }

   // pede a lista de templates de resources ao servidor
   @Override
   public List<ResourceTemplate> listResourceTemplates() {
      return client.listResourceTemplates().resourceTemplates();
   }

   // pede ao servidor o conteúdo de um resource
   @Override
   public ReadResourceResult readResource(String uri) {
      return client.readResource(ReadResourceRequest.builder(uri).build());
   }

   // pede a lista de prompts ao servidor
   @Override
   public List<Prompt> listPrompts() {
      return client.listPrompts().prompts();
   }

   // pede ao servidor o texto de um prompt
   @Override
   public GetPromptResult getPrompt(String name,
                                    Map<String, Object> arguments) {
      // monta o pedido com o nome do prompt e os argumentos
      return client.getPrompt(GetPromptRequest.builder(name)
                                              .arguments(arguments)
                                              .build());
   }

   // fecha a conexão; o servidor encerra sozinho em seguida
   @Override
   public void close() {
      client.closeGracefully();
   }
}
