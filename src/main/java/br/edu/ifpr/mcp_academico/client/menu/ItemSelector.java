// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// importa as classes do Java para listas, valores opcionais e funções
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// importa a classe de entrada e saída do cliente
import br.edu.ifpr.mcp_academico.client.ClientConsole;

// escolha de um item em uma lista, pelo número exibido ou pelo nome
final class ItemSelector {
   // construtor privado: esta classe não deve ser criada com "new"
   private ItemSelector() {}

   // procura o item pelo número (começando em 1) ou pelo nome
   static <T> Optional<T> select(List<T> items, String command,
                                 Function<T, String> nameOf) {
      try {
         // tenta ler o comando como número; o índice da lista começa em 0
         int index = Integer.parseInt(command) - 1;

         // se o número existe na lista, devolve o item; senão, vazio
         return index >= 0 && index < items.size()
                   ? Optional.of(items.get(index)) : Optional.empty();
      }
      catch (NumberFormatException ignored) {
         // o comando pode ser o nome do item
         return items.stream()
                     .filter(item -> nameOf.apply(item)
                                           .equalsIgnoreCase(command))
                     .findFirst();
      }
   }

   // exibe a lista ao usuário e retorna o item escolhido, se houver
   static <T> Optional<T> pick(ClientConsole console, String title,
                               List<T> items, Function<T, String> nameOf,
                               Function<T, String> descriptionOf) {
      // título da lista
      console.println();
      console.println(title + ":");

      // lista vazia: avisa e volta
      if (items.isEmpty()) {
         console.println(" Nenhum item disponível.");
         return Optional.empty();
      }

      // mostra cada item com número, nome e descrição
      for (int i = 0; i < items.size(); i++) {
         // pega o item da posição atual
         T item = items.get(i);

         // número e nome
         console.println(" " + (i + 1) + " - " + nameOf.apply(item));

         // descrição, na linha de baixo, se houver
         if (descriptionOf.apply(item) != null)
            console.println("     " + descriptionOf.apply(item));
      }

      // lê a escolha; ENTER sem texto significa voltar
      Optional<String> command = console.readLine(
                                 "Número ou nome (ENTER para voltar): ")
                                        .filter(text -> !text.isBlank());
      // procura o item escolhido
      Optional<T>      choice  = command.flatMap(text ->
                                              select(items,text,nameOf));

      // digitou algo que não existe: avisa
      if (command.isPresent() && choice.isEmpty())
         console.println("Item não encontrado: " + command.get());

      // devolve o item escolhido (ou vazio)
      return choice;
   }
}
