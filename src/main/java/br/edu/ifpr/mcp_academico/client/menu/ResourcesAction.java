// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// importa as classes do Java para listas e valores opcionais
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// importa as classes do cliente usadas por esta opção
import br.edu.ifpr.mcp_academico.client.ClientConsole;
import br.edu.ifpr.mcp_academico.client.McpGateway;
import br.edu.ifpr.mcp_academico.client.ResultPrinter;
// importa a classe da biblioteca MCP que lê as variáveis de um endereço
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManagerFactory;

// opção "Resources" do menu: lista os resources e mostra o escolhido
public final class ResourcesAction implements MenuAction {
   // resources e resource templates são exibidos em uma única lista; cada
   // item guarda o endereço e a descrição
   private record Item(String uri, String description) {}

   // objeto que conversa com o servidor
   private final McpGateway    gateway;
   // objeto que lê o teclado e escreve na tela
   private final ClientConsole console;
   // objeto que mostra a resposta
   private final ResultPrinter printer;

   // recebe os objetos que esta opção usa
   public ResourcesAction(McpGateway gateway, ClientConsole console,
                          ResultPrinter printer) {
      // guarda cada um para usar nos outros métodos
      this.gateway = gateway;
      this.console = console;
      this.printer = printer;
   }

   // texto da opção no menu
   @Override
   public String label() {
      return "Resources";
   }

   // lista os resources, lê o escolhido e mostra o conteúdo
   @Override
   public void execute() {
      // mostra a lista; se o usuário escolher um item, completa o endereço
      // e pede o conteúdo ao servidor
      ItemSelector.pick(console,label(),items(),Item::uri,Item::description)
                  .flatMap(item -> resolve(item.uri()))
                  .ifPresent(uri -> printer.print(gateway.readResource(uri)));
   }

   // junta os resources e os templates em uma lista só
   private List<Item> items() {
      // lista que vai guardar os itens
      List<Item> items = new ArrayList<>();

      // adiciona os resources
      gateway.listResources().forEach(resource ->
             items.add(new Item(resource.uri(),resource.description())));
      // adiciona os templates
      gateway.listResourceTemplates().forEach(template ->
             items.add(new Item(template.uriTemplate(),template.description())));

      // devolve a lista
      return items;
   }

   // substitui as variáveis de um resource template pelos valores do usuário
   private Optional<String> resolve(String uri) {
      // pega os nomes das variáveis do endereço, como "id"
      List<String> variables = new DefaultMcpUriTemplateManagerFactory()
                                                     .create(uri)
                                                     .getVariableNames();
      // endereço que vai sendo completado
      String       resolved  = uri;

      // pergunta o valor de cada variável
      for (String variable : variables) {
         // lê o valor digitado
         Optional<String> value = console.readLine(variable + ": ");

         // entrada terminou: desiste
         if (value.isEmpty())
            return Optional.empty();

         // troca "{id}" pelo valor digitado
         resolved = resolved.replace("{" + variable + "}",value.get());
      }

      // devolve o endereço completo
      return Optional.of(resolved);
   }
}
