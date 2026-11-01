// pacote (pasta) com as classes que leem os argumentos digitados
package br.edu.ifpr.mcp_academico.client.arguments;

// importa as classes do Java para listas e mapas
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// importa as classes da biblioteca Jackson, que lê JSON
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// reúne os conversores de cada tipo do esquema JSON. Qualquer tipo não previsto
// é tratado como um valor em JSON, o que permite executar toda e qualquer
// ferramenta
public final class ValueParsers {
   // nome usado para parâmetros com lista de valores permitidos
   static final String ENUM = "enum";
   // nome usado para tipos não previstos, lidos como JSON
   static final String JSON = "json";

   // objeto que lê textos em JSON
   private static final JsonMapper MAPPER = new JsonMapper();

   // liga cada tipo do esquema ao seu conversor
   private static final Map<String, ValueParser> PARSERS = Map.of(
           // texto: usa o que foi digitado, sem mudanças
           "string", (text,schema) -> text,
           // número inteiro
           "integer",(text,schema) -> parseInteger(text),
           // número com casas decimais
           "number", (text,schema) -> parseNumber(text),
           // sim ou não
           "boolean",(text,schema) -> parseBoolean(text),
           // lista
           "array",  ValueParsers::parseArray,
           // um dos valores permitidos
           ENUM,     ValueParsers::parseEnum);

   // construtor privado: esta classe não deve ser criada com "new"
   private ValueParsers() {}

   // converte o texto usando o conversor do tipo do parâmetro; tipos não
   // previstos são lidos como JSON
   public static Object parse(String text, Map<?, ?> schema) {
      return PARSERS.getOrDefault(typeOf(schema),(value,ignored) ->
                                                  parseJson(value))
                    .parse(text,schema);
   }

   // descobre o tipo do parâmetro. O tipo pode ser um texto, uma lista de
   // tipos, como ["string","null"], ou não existir, como em esquemas que
   // usam "anyOf"
   static String typeOf(Map<?, ?> schema) {
      // com lista de valores permitidos, o tipo é "enum"
      if (schema.containsKey(ENUM))
         return ENUM;

      // pega o campo "type" do esquema
      Object type = schema.get("type");

      // o tipo é um texto, como "integer": devolve ele
      if (type instanceof String name)
         return name;

      // o tipo é uma lista: devolve o primeiro que não seja "null"
      if (type instanceof List<?> names)
         return names.stream()
                     .filter(name -> !"null".equals(name))
                     .map(String::valueOf)
                     .findFirst()
                     .orElse(JSON);

      // sem tipo: trata como JSON
      return JSON;
   }

   // converte o texto em número inteiro
   private static Long parseInteger(String text) {
      // tenta converter; se não for número inteiro, avisa o erro
      try { return Long.valueOf(text); }
      catch (NumberFormatException ex) {
         throw new IllegalArgumentException("informe um número inteiro");
      }
   }

   // converte o texto em número decimal, aceitando vírgula ou ponto
   private static Double parseNumber(String text) {
      // troca a vírgula por ponto e tenta converter
      try { return Double.valueOf(text.replace(',','.')); }
      catch (NumberFormatException ex) {
         throw new IllegalArgumentException("informe um número");
      }
   }

   // converte o texto em sim (true) ou não (false)
   private static Boolean parseBoolean(String text) {
      // aceita "true" ou "sim" como verdadeiro
      if ("true".equalsIgnoreCase(text) || "sim".equalsIgnoreCase(text))
         return true;

      // aceita "false", "não" ou "nao" como falso
      if ("false".equalsIgnoreCase(text) ||
          "não".equalsIgnoreCase(text) || "nao".equalsIgnoreCase(text))
         return false;

      // qualquer outro texto é inválido
      throw new IllegalArgumentException("informe true/false ou sim/não");
   }

   // confere se o texto é um dos valores permitidos
   private static Object parseEnum(String text, Map<?, ?> schema) {
      // pega a lista de valores permitidos
      List<?> values = (List<?>) schema.get(ENUM);

      // procura o valor sem diferenciar maiúsculas e minúsculas; se não
      // achar, avisa o erro com a lista de opções
      return values.stream()
                   .filter(value -> String.valueOf(value)
                                          .equalsIgnoreCase(text))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(
                                         "escolha um dos valores " + values));
   }

   // converte uma lista. Aceita uma lista em JSON ou itens separados por
   // vírgulas
   private static Object parseArray(String text, Map<?, ?> schema) {
      // começa com "[": lê como JSON
      if (text.startsWith("["))
         return parseJson(text);

      // pega o esquema dos itens (ou um esquema vazio)
      Map<?, ?> items = schema.get("items") instanceof Map<?, ?> map ? map
                                                                     : Map.of();

      // separa o texto nas vírgulas, tira os espaços e converte cada item
      return Arrays.stream(text.split(","))
                   .map(String::trim)
                   .map(item -> parse(item,items))
                   .toList();
   }

   // lê o texto como JSON
   private static Object parseJson(String text) {
      // tenta ler; se o JSON estiver errado, avisa o erro
      try { return MAPPER.readValue(text,Object.class); }
      catch (JacksonException ex) {
         throw new IllegalArgumentException("informe um valor em JSON válido");
      }
   }
}
