// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa as classes do Java para listas e mapas
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// monta o esquema JSON dos parâmetros de uma ferramenta. O esquema diz ao
// cliente quais parâmetros existem, de que tipo são e quais são obrigatórios
public final class JsonSchemaBuilder {
   // LinkedHashMap mantém a ordem de declaração, que é a ordem usada pelo
   // cliente MCP ao solicitar os parâmetros ao usuário
   private final Map<String, Object> properties = new LinkedHashMap<>();
   // nomes dos parâmetros obrigatórios
   private final List<String>        required   = new ArrayList<>();

   // construtor privado: use o método "object()" para começar
   private JsonSchemaBuilder() {}

   // começa a montar o esquema de um objeto (o conjunto de parâmetros)
   public static JsonSchemaBuilder object() {
      return new JsonSchemaBuilder();
   }

   // adiciona um parâmetro obrigatório
   public JsonSchemaBuilder required(String name, Map<String, Object> schema) {
      // marca o nome como obrigatório
      required.add(name);

      // adiciona o parâmetro do mesmo jeito que um opcional
      return optional(name,schema);
   }

   // adiciona um parâmetro opcional
   public JsonSchemaBuilder optional(String name, Map<String, Object> schema) {
      // guarda o parâmetro e o seu esquema
      properties.put(name,schema);

      // devolve o próprio construtor, para encadear chamadas
      return this;
   }

   // termina e devolve o esquema pronto
   public Map<String, Object> build() {
      // mapa que vai guardar o esquema
      Map<String, Object> schema = new LinkedHashMap<>();

      // o conjunto de parâmetros é um objeto JSON
      schema.put("type","object");
      // lista de parâmetros (uma cópia, para não ser alterada depois)
      schema.put("properties",new LinkedHashMap<>(properties));
      // só coloca a lista de obrigatórios se houver algum
      if (!required.isEmpty())
         schema.put("required",List.copyOf(required));
      // não aceita parâmetros que não foram declarados
      schema.put("additionalProperties",false);

      // devolve o esquema
      return schema;
   }

   // esquema de um parâmetro de texto
   public static Map<String, Object> string(String description) {
      return typed("string",description);
   }

   // esquema de um parâmetro de número inteiro
   public static Map<String, Object> integer(String description) {
      return typed("integer",description);
   }

   // esquema de um parâmetro de número (inteiro ou decimal)
   public static Map<String, Object> number(String description) {
      return typed("number",description);
   }

   // esquema de um parâmetro de sim ou não (true ou false)
   public static Map<String, Object> bool(String description) {
      return typed("boolean",description);
   }

   // esquema de um texto que só aceita alguns valores, como "texto" e "json"
   public static Map<String, Object> enumeration(String description,
                                                 String... values) {
      // começa como um parâmetro de texto
      Map<String, Object> schema = string(description);

      // acrescenta a lista de valores permitidos
      schema.put("enum",List.of(values));

      // devolve o esquema
      return schema;
   }

   // esquema de uma lista, em que "items" descreve cada item
   public static Map<String, Object> arrayOf(String description,
                                             Map<String, Object> items) {
      // começa como um parâmetro do tipo lista
      Map<String, Object> schema = typed("array",description);

      // diz como é cada item da lista
      schema.put("items",items);

      // devolve o esquema
      return schema;
   }

   // monta o esquema básico com o tipo e a descrição
   private static Map<String, Object> typed(String type, String description) {
      // mapa que vai guardar o esquema
      Map<String, Object> schema = new LinkedHashMap<>();

      // tipo do parâmetro, como "string" ou "integer"
      schema.put("type",type);
      // texto que explica o parâmetro
      schema.put("description",description);

      // devolve o esquema
      return schema;
   }
}
