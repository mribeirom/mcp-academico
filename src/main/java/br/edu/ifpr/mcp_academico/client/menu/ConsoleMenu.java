// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a classe de entrada e saída do cliente
import br.edu.ifpr.mcp_academico.client.ClientConsole;

// menu principal do cliente, mostrado no terminal
public final class ConsoleMenu {
   // comando que encerra o cliente
   private static final String EXIT = "sair";

   // objeto que lê o teclado e escreve na tela
   private final ClientConsole    console;
   // opções do menu
   private final List<MenuAction> actions;

   // recebe o objeto de entrada e saída e as opções do menu
   public ConsoleMenu(ClientConsole console, List<MenuAction> actions) {
      // guarda os dois para usar nos outros métodos
      this.console = console;
      this.actions = actions;
   }

   // executa até que o usuário saia ou que a entrada termine
   public void run() {
      // repete sem parar, até um "return"
      while (true) {
         // mostra as opções
         show();

         // lê o comando digitado
         Optional<String> command = console.readLine("> ");

         // entrada terminou ou o usuário digitou "sair": encerra
         if (command.isEmpty() || EXIT.equalsIgnoreCase(command.get()))
            return;

         // se o usuário digitou algo, executa a opção
         if (!command.get().isBlank())
            perform(command.get());
      }
   }

   // mostra o menu na tela
   private void show() {
      // cabeçalho
      console.println();
      console.println("========================================");
      console.println(" CLIENTE MCP ACADÊMICO");
      console.println("========================================");

      // uma linha por opção, numeradas a partir de 1
      for (int i = 0; i < actions.size(); i++)
         console.println(" " + (i + 1) + " - " + actions.get(i).label());

      // opção de sair
      console.println(" " + EXIT + " - encerrar o cliente");
      console.println();
   }

   // executa a opção escolhida pelo número ou pelo nome
   private void perform(String command) {
      // procura a opção correspondente ao comando
      Optional<MenuAction> action = ItemSelector.select(actions,command,
                                                        MenuAction::label);

      // opção inexistente: avisa e volta ao menu
      if (action.isEmpty()) {
         console.println("Opção não encontrada: " + command);
         return;
      }
      // uma falha em uma operação não deve encerrar o cliente
      try { action.get().execute(); }
      catch (RuntimeException ex) {
         console.println("Erro: " + ex.getMessage());
      }
   }
}
