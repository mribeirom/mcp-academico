// pacote (pasta) com as classes que leem os argumentos digitados
package br.edu.ifpr.mcp_academico.client.arguments;

// importa as classes do Java para listas, mapas e valores opcionais
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// importa a classe de entrada e saída do cliente
import br.edu.ifpr.mcp_academico.client.ClientConsole;

// solicita ao usuário os argumentos definidos em um esquema JSON, sem conhecer
// nenhuma ferramenta: o que é perguntado depende somente do esquema
public final class ArgumentPrompter {
   // nome do campo do esquema que lista os parâmetros
   private static final String PROPERTIES = "properties";

   // objeto que lê o teclado e escreve na tela
   private final ClientConsole console;

   // recebe o objeto de entrada e saída
   public ArgumentPrompter(ClientConsole console) {
      // guarda esse objeto para usar nos outros métodos
      this.console = console;
   }

   // pergunta todos os argumentos do esquema e devolve os valores digitados
   public Map<String, Object> readArguments(Map<String, Object> schema) {
      // sem esquema, não há o que perguntar
      return schema == null ? Map.of() : readObject(schema);
   }

   // pergunta cada parâmetro de um objeto do esquema
   private Map<String, Object> readObject(Map<?, ?> schema) {
      // mapa que vai guardar os valores digitados
      Map<String, Object> values = new LinkedHashMap<>();

      // se o esquema não tem parâmetros, devolve o mapa vazio
      if (!(schema.get(PROPERTIES) instanceof Map<?, ?> properties))
         return values;

      // pega a lista de parâmetros obrigatórios (ou uma lista vazia)
      List<?> required = schema.get("required") instanceof List<?> list
                            ? list : List.of();

      // pergunta cada parâmetro, na ordem do esquema
      properties.forEach((key,value) -> {
         // nome do parâmetro
         String name   = String.valueOf(key);
         // pergunta o valor ao usuário
         Object result = readProperty(name,(Map<?, ?>) value,
                                      required.contains(name));
         // guarda o valor, se o usuário informou algum
         if (result != null)
            values.put(name,result);
      });

      // devolve os valores
      return values;
   }

   // pergunta um parâmetro, conforme o seu tipo
   private Object readProperty(String name, Map<?, ?> schema,
                               boolean required) {
      // mostra o nome, o tipo e a descrição do parâmetro
      describe(name,schema,required);

      // lista de objetos (como as notas): pergunta item por item
      if (isObjectArray(schema))
         return readObjectArray(name,(Map<?, ?>) schema.get("items"));

      // objeto com parâmetros próprios: pergunta cada um deles
      if (schema.get(PROPERTIES) instanceof Map<?, ?>)
         return readObject(schema);

      // valor simples: lê uma linha e converte
      return readValue(schema,required);
   }

   // mostra ao usuário as informações de um parâmetro
   private void describe(String name, Map<?, ?> schema, boolean required) {
      // linha em branco para separar
      console.println();
      // nome, tipo e se é obrigatório, como
      // "'aluno_id' [integer] (obrigatório)"
      console.println("'" + name + "' [" + ValueParsers.typeOf(schema) + "] " +
                      (required ? "(obrigatório)" : "(opcional)"));

      // mostra a descrição, se houver
      if (schema.get("description") != null)
         console.println("Descrição: " + schema.get("description"));

      // mostra os valores permitidos, se houver
      if (schema.get(ValueParsers.ENUM) != null)
         console.println("Valores: " + schema.get(ValueParsers.ENUM));

      // para listas simples, explica como digitar os itens
      if ("array".equals(ValueParsers.typeOf(schema)) && !isObjectArray(schema))
         console.println("Formato: itens separados por vírgulas ou lista em " +
                         "JSON, como [1,2] ([] para vazia)");
   }

   // responde se o parâmetro é uma lista de objetos
   private boolean isObjectArray(Map<?, ?> schema) {
      // é lista e cada item tem parâmetros próprios
      return "array".equals(ValueParsers.typeOf(schema)) &&
             schema.get("items") instanceof Map<?, ?> items &&
             items.get(PROPERTIES) instanceof Map<?, ?>;
   }

   // pergunta os itens de uma lista de objetos, um de cada vez
   private List<Object> readObjectArray(String name, Map<?, ?> items) {
      // lista que vai guardar os itens
      List<Object> values = new ArrayList<>();

      // enquanto o usuário responder "sim", pergunta mais um item
      while (console.readLine("Adicionar item a '" + name + "'? (sim/não): ")
                    .filter(answer -> answer.toLowerCase().startsWith("s"))
                    .isPresent())
         values.add(readObject(items));

      // devolve os itens
      return values;
   }

   // lê um valor simples, repetindo a pergunta até ele ser válido
   private Object readValue(Map<?, ?> schema, boolean required) {
      // repete até receber um valor válido
      while (true) {
         // lê a linha digitada
         Optional<String> line = console.readLine("Valor: ");
         // a entrada terminou ou o parâmetro opcional foi deixado em branco
         if (line.isEmpty() || (line.get().isBlank() && !required))
            return null;

         // obrigatório em branco: avisa e pergunta de novo
         if (line.get().isBlank()) {
            console.println("Este parâmetro é obrigatório.");
            continue;
         }
         // tenta converter o texto para o tipo do parâmetro
         try { return ValueParsers.parse(line.get(),schema); }
         // valor inválido: avisa e pergunta de novo
         catch (IllegalArgumentException ex) {
            console.println("Valor inválido: " + ex.getMessage());
         }
      }
   }
}
