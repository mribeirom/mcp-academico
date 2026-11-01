// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// importa as classes do Java para listas e mapas
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// importa as classes do cliente usadas por esta opção
import br.edu.ifpr.mcp_academico.client.ClientConsole;
import br.edu.ifpr.mcp_academico.client.McpGateway;
import br.edu.ifpr.mcp_academico.client.ResultPrinter;
import br.edu.ifpr.mcp_academico.client.arguments.ArgumentPrompter;
// importa as classes da biblioteca MCP que descrevem prompts
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

// opção "Prompts" do menu: lista os prompts e mostra o texto do escolhido
public final class PromptsAction implements MenuAction {
   // objeto que conversa com o servidor
   private final McpGateway       gateway;
   // objeto que lê o teclado e escreve na tela
   private final ClientConsole    console;
   // objeto que pergunta os argumentos
   private final ArgumentPrompter prompter;
   // objeto que mostra a resposta
   private final ResultPrinter    printer;

   // recebe os objetos que esta opção usa
   public PromptsAction(McpGateway gateway, ClientConsole console,
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
      return "Prompts";
   }

   // lista os prompts e executa o escolhido
   @Override
   public void execute() {
      // mostra a lista de prompts do servidor e, se o usuário escolher um,
      // executa esse prompt
      ItemSelector.pick(console,label(),gateway.listPrompts(),Prompt::name,
                        Prompt::description)
                  .ifPresent(this::run);
   }

   // pergunta os argumentos, pede o texto ao servidor e mostra a resposta
   private void run(Prompt prompt) {
      // pergunta os argumentos do prompt
      Map<String, Object> arguments =
                                prompter.readArguments(schemaOf(prompt));

      // pede o texto ao servidor e mostra na tela
      printer.print(gateway.getPrompt(prompt.name(),arguments));
   }

   // os argumentos de um prompt são sempre textos: descrevê-los como um esquema
   // JSON permite reaproveitar a leitura de argumentos das ferramentas
   private Map<String, Object> schemaOf(Prompt prompt) {
      // parâmetros do esquema
      Map<String, Object> properties = new LinkedHashMap<>();
      // nomes dos parâmetros obrigatórios
      List<String>        required   = new ArrayList<>();

      // transforma cada argumento do prompt em um parâmetro do esquema
      for (PromptArgument argument : prompt.arguments()) {
         // esquema do parâmetro
         Map<String, Object> property = new LinkedHashMap<>();

         // todo argumento de prompt é texto
         property.put("type","string");
         // copia a descrição do argumento
         property.put("description",argument.description());
         // adiciona o parâmetro ao esquema
         properties.put(argument.name(),property);

         // se o argumento é obrigatório, anota o nome dele
         if (Boolean.TRUE.equals(argument.required()))
            required.add(argument.name());
      }

      // devolve o esquema montado
      return Map.of("properties",properties,"required",required);
   }
}
