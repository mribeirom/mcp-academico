// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa as classes do cliente usadas por esta opção
import br.edu.ifpr.mcp_academico.client.ClientConsole;
import br.edu.ifpr.mcp_academico.client.McpGateway;
import br.edu.ifpr.mcp_academico.client.ResultPrinter;
import br.edu.ifpr.mcp_academico.client.arguments.ArgumentPrompter;
// importa a classe da biblioteca MCP que descreve uma ferramenta
import io.modelcontextprotocol.spec.McpSchema.Tool;

// opção "Ferramentas" do menu: lista as ferramentas e executa a escolhida
public final class ToolsAction implements MenuAction {
   // objeto que conversa com o servidor
   private final McpGateway       gateway;
   // objeto que lê o teclado e escreve na tela
   private final ClientConsole    console;
   // objeto que pergunta os argumentos
   private final ArgumentPrompter prompter;
   // objeto que mostra a resposta
   private final ResultPrinter    printer;

   // recebe os objetos que esta opção usa
   public ToolsAction(McpGateway gateway, ClientConsole console,
                      ArgumentPrompter prompter, ResultPrinter printer) {
      // guarda cada um para usar nos outros métodos
      this.gateway  = gateway;
      this.console  = console;
      this.prompter = prompter;
      this.printer  = printer;
   }

   // texto da opção no menu
   @Override
   public String label() {
      return "Ferramentas";
   }

   // lista as ferramentas e executa a escolhida
   @Override
   public void execute() {
      // a lista é sempre recuperada do servidor: o cliente não conhece as
      // ferramentas disponíveis
      ItemSelector.pick(console,label(),gateway.listTools(),Tool::name,
                        Tool::description)
                  .ifPresent(this::run);
   }

   // pergunta os argumentos, executa a ferramenta e mostra o resultado
   private void run(Tool tool) {
      // pergunta os argumentos conforme o esquema da ferramenta
      Map<String, Object> arguments =
                                  prompter.readArguments(tool.inputSchema());

      // executa a ferramenta no servidor e mostra o resultado
      printer.print(gateway.callTool(tool.name(),arguments));
   }
}
