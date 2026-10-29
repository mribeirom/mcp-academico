// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa as classes da biblioteca Jackson, que converte objetos em JSON
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

// converte os objetos da aplicação para JSON, usando nomes de campos no mesmo
// padrão dos parâmetros das ferramentas (snake_case)
public final class JsonSupport {
   // conversor configurado uma única vez: nomes como "alunoId" viram
   // "aluno_id", e o JSON sai com quebras de linha e recuos
   private static final JsonMapper MAPPER =
       JsonMapper.builder()
                 .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                 .enable(SerializationFeature.INDENT_OUTPUT)
                 .build();

   // construtor privado: esta classe não deve ser criada com "new"
   private JsonSupport() {}

   // converte um objeto em texto JSON
   public static String toJson(Object value) {
      return MAPPER.writeValueAsString(value);
   }

   // converte um objeto em mapa (nome do campo ligado ao seu valor)
   public static Map<String, Object> toMap(Object value) {
      return MAPPER.convertValue(value,new TypeReference<>() {});
   }
}
