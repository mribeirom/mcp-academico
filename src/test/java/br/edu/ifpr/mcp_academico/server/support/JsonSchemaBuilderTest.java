package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonSchemaBuilderTest {
   @Test
   void deveCriarObjetoSemPropriedadesAdicionais() {
      Map<String, Object> schema = JsonSchemaBuilder.object().build();

      assertEquals("object",schema.get("type"));
      assertEquals(false,schema.get("additionalProperties"));
   }

   @Test
   void naoDeveDeclararRequiredQuandoNaoHaObrigatorios() {
      Map<String, Object> schema =
            JsonSchemaBuilder.object()
                             .optional("a",JsonSchemaBuilder.string("texto"))
                             .build();

      assertFalse(schema.containsKey("required"));
   }

   @Test
   void deveListarSomenteOsParametrosObrigatorios() {
      Map<String, Object> schema =
            JsonSchemaBuilder.object()
                             .required("a",JsonSchemaBuilder.string("texto"))
                             .optional("b",JsonSchemaBuilder.string("texto"))
                             .build();

      assertEquals(List.of("a"),schema.get("required"));
   }

   @Test
   @SuppressWarnings("unchecked")
   void deveManterAOrdemDeDeclaracaoDasPropriedades() {
      Map<String, Object> schema =
            JsonSchemaBuilder.object()
                             .required("z",JsonSchemaBuilder.string("texto"))
                             .optional("m",JsonSchemaBuilder.string("texto"))
                             .required("a",JsonSchemaBuilder.string("texto"))
                             .build();
      Map<String, Object> properties =
                                  (Map<String, Object>) schema.get("properties");

      assertEquals(List.of("z","m","a"),List.copyOf(properties.keySet()));
   }

   @Test
   void deveCriarEsquemasDosTiposBasicos() {
      assertEquals("string",JsonSchemaBuilder.string("d").get("type"));
      assertEquals("integer",JsonSchemaBuilder.integer("d").get("type"));
      assertEquals("number",JsonSchemaBuilder.number("d").get("type"));
      assertEquals("boolean",JsonSchemaBuilder.bool("d").get("type"));
      assertEquals("d",JsonSchemaBuilder.string("d").get("description"));
   }

   @Test
   void deveCriarEsquemaDeEnumeracao() {
      Map<String, Object> schema =
                    JsonSchemaBuilder.enumeration("formato","texto","json");

      assertEquals("string",schema.get("type"));
      assertEquals(List.of("texto","json"),schema.get("enum"));
   }

   @Test
   void deveCriarEsquemaDeArrayDeObjetos() {
      Map<String, Object> item   =
            JsonSchemaBuilder.object()
                             .required("id",JsonSchemaBuilder.integer("id"))
                             .build();
      Map<String, Object> schema = JsonSchemaBuilder.arrayOf("itens",item);

      assertEquals("array",schema.get("type"));
      assertEquals(item,schema.get("items"));
   }
}
