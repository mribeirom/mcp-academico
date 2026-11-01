package br.edu.ifpr.mcp_academico.client.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueParsersTest {
   private static Map<String, Object> type(String name) {
      return Map.of("type",name);
   }

   private static void assertInvalid(String text, Map<?, ?> schema) {
      assertThrows(IllegalArgumentException.class,
                   () -> ValueParsers.parse(text,schema));
   }

   @Test
   void deveConverterTexto() {
      assertEquals("João",ValueParsers.parse("João",type("string")));
   }

   @Test
   void deveConverterInteiro() {
      assertEquals(7L,ValueParsers.parse("7",type("integer")));
      assertInvalid("7.5",type("integer"));
      assertInvalid("abc",type("integer"));
   }

   @Test
   void deveConverterNumeroComPontoOuVirgula() {
      assertEquals(7.5,ValueParsers.parse("7.5",type("number")));
      assertEquals(7.5,ValueParsers.parse("7,5",type("number")));
      assertInvalid("abc",type("number"));
   }

   @ParameterizedTest
   @ValueSource(strings = {"true","sim","SIM","True"})
   void deveConverterVerdadeiro(String text) {
      assertEquals(true,ValueParsers.parse(text,type("boolean")));
   }

   @ParameterizedTest
   @ValueSource(strings = {"false","não","nao","NÃO"})
   void deveConverterFalso(String text) {
      assertEquals(false,ValueParsers.parse(text,type("boolean")));
   }

   @Test
   void deveRejeitarBooleanoInvalido() {
      assertInvalid("talvez",type("boolean"));
   }

   @Test
   void deveAceitarSomenteValoresDaEnumeracao() {
      Map<String, Object> schema = Map.of("type","string",
                                          "enum",List.of("texto","json"));

      assertEquals("json",ValueParsers.parse("JSON",schema));
      assertInvalid("pdf",schema);
   }

   @Test
   void deveManterOTipoDosValoresDaEnumeracao() {
      assertEquals(2,ValueParsers.parse("2",Map.of("enum",List.of(1,2))));
   }

   @Test
   void deveConverterListaSeparadaPorVirgulasPeloTipoDosItens() {
      Map<String, Object> schema = Map.of("type","array",
                                          "items",type("integer"));

      assertEquals(List.of(1L,2L,3L),ValueParsers.parse("1, 2,3",schema));
      assertInvalid("1,abc",schema);
   }

   @Test
   void deveAceitarListaEmJson() {
      assertEquals(List.of(),ValueParsers.parse("[]",type("array")));
      assertEquals(List.of(1,2),ValueParsers.parse("[1,2]",type("array")));
      assertInvalid("[1,",type("array"));
   }

   @Test
   void deveConverterListaSemTipoDosItensComoJson() {
      assertEquals(List.of(1,"a"),ValueParsers.parse("1,\"a\"",type("array")));
   }

   @Test
   void deveConverterObjetoEmJson() {
      assertEquals(Map.of("a",1),
                   ValueParsers.parse("{\"a\":1}",type("object")));
      assertInvalid("{a",type("object"));
   }

   // esquemas que o menu não conhece são preenchidos em JSON
   @Test
   void deveTratarTipoDesconhecidoComoJson() {
      assertEquals(Map.of("x",true),
                   ValueParsers.parse("{\"x\":true}",type("novo-tipo")));
      assertEquals(5,ValueParsers.parse("5",Map.of("anyOf",List.of())));
   }

   @Test
   void deveIdentificarOTipoDoEsquema() {
      assertEquals("string",ValueParsers.typeOf(type("string")));
      assertEquals(ValueParsers.ENUM,
                   ValueParsers.typeOf(Map.of("enum",List.of("a"))));
      assertEquals(ValueParsers.JSON,ValueParsers.typeOf(Map.of()));
   }

   @Test
   void deveIdentificarTipoEmListaIgnorandoNull() {
      assertEquals("integer",ValueParsers.typeOf(
                     Map.of("type",Arrays.asList("null","integer"))));
      assertEquals(ValueParsers.JSON,ValueParsers.typeOf(
                     Map.of("type",List.of("null"))));
   }
}
