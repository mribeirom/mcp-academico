// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa as classes do Java para arquivos, caminhos e codificação de texto
import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

// importa as classes que pedem argumentos e montam o menu
import br.edu.ifpr.mcp_academico.client.arguments.ArgumentPrompter;
import br.edu.ifpr.mcp_academico.client.menu.ConsoleMenu;
import br.edu.ifpr.mcp_academico.client.menu.PromptsAction;
import br.edu.ifpr.mcp_academico.client.menu.ResourcesAction;
import br.edu.ifpr.mcp_academico.client.menu.ToolsAction;

// classe principal do cliente MCP: é ela que o comando "java -jar" executa
public class McpAcademicoClient {
   // construtor privado: esta classe só tem métodos estáticos
   private McpAcademicoClient() {}

   // cria os objetos do menu principal, que recebem suas dependências pelo
   // construtor
   public static ConsoleMenu menu(McpGateway gateway, ClientConsole console) {
      // objeto que pergunta os argumentos ao usuário
      ArgumentPrompter prompter = new ArgumentPrompter(console);
      // objeto que mostra as respostas do servidor
      ResultPrinter    printer  = new ResultPrinter(console);

      // monta o menu com as três opções: ferramentas, resources e prompts
      return new ConsoleMenu(console,List.of(
                     new ToolsAction(gateway,console,prompter,printer),
                     new ResourcesAction(gateway,console,printer),
                     new PromptsAction(gateway,console,prompter,printer)));
   }

   // diretório que contém o arquivo JAR do cliente
   private static Path clientDirectory() throws URISyntaxException {
      // pergunta ao Java de onde esta classe foi carregada (o arquivo JAR) e
      // pega a pasta desse arquivo
      return Path.of(McpAcademicoClient.class.getProtectionDomain()
                                             .getCodeSource()
                                             .getLocation()
                                             .toURI())
                 .getParent();
   }

   // o console do Windows não usa UTF-8, que é o padrão do Java: sem esta
   // informação os acentos digitados pelo usuário seriam corrompidos
   private static Charset consoleCharset() {
      // lê a codificação usada pelo terminal
      String encoding = System.getProperty("stdin.encoding");

      // usa a codificação do terminal ou, se não houver, a padrão do Java
      return encoding != null ? Charset.forName(encoding)
                              : Charset.defaultCharset();
   }

   // ponto de partida do programa
   public static void main(String... args) throws URISyntaxException {
      // encontra o arquivo JAR do servidor
      File          serverJar = ServerLocator.locate(System.getenv(),
                                                     clientDirectory());
      // prepara a entrada (teclado) e a saída (tela)
      ClientConsole console   = new ClientConsole(System.in,consoleCharset(),
                                                  System.out);

      // inicia o servidor e conecta a ele; o "try" garante que a conexão
      // será fechada no final
      try (SdkMcpGateway gateway = SdkMcpGateway.connect(serverJar,
                                                         System.getenv())) {
         // mostra o menu até o usuário digitar "sair"
         menu(gateway,console).run();
      }
   }
}
