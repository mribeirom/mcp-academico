package br.edu.ifpr.mcp_academico.client.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.client.ScriptedConsole;

// os esquemas usados aqui não existem no servidor: o cliente deve ser capaz de
// executar toda e qualquer ferramenta somente a partir do esquema JSON
class ArgumentPrompterTest {
   private static Map<String, Object> schema(Map<String, Object> properties,
                                             String... required) {
      return Map.of("type","object","properties",properties,
                    "required",List.of(required));
   }

   private static Map<String, Object> read(Map<String, Object> schema,
                                           String... lines) {
      ScriptedConsole scripted = new ScriptedConsole(lines);

      return new ArgumentPrompter(scripted.console()).readArguments(schema);
   }

   @Test
   void deveRetornarVazioSemEsquema() {
      assertEquals(Map.of(),read(null));
   }

   @Test
   void deveRetornarVazioSemPropriedades() {
      assertEquals(Map.of(),read(Map.of("type","object")));
   }

   @Test
   void deveSolicitarOsParametrosNaOrdemDoEsquema() {
      Map<String, Object> properties = new LinkedHashMap<>();

      properties.put("z",Map.of("type","string"));
      properties.put("a",Map.of("type","integer"));

      Map<String, Object> values = read(schema(properties,"z","a"),"João","7");

      assertEquals(List.of("z","a"),List.copyOf(values.keySet()));
      assertEquals("João",values.get("z"));
      assertEquals(7L,values.get("a"));
   }

   @Test
   void deveOmitirParametroOpcionalEmBranco() {
      assertEquals(Map.of(),read(schema(Map.of("a",Map.of("type","string"))),
                                 ""));
   }

   @Test
   void deveInsistirEmParametroObrigatorio() {
      ScriptedConsole scripted = new ScriptedConsole("","ok");
      Map<String, Object> values =
           new ArgumentPrompter(scripted.console()).readArguments(
                schema(Map.of("a",Map.of("type","string")),"a"));

      assertEquals("ok",values.get("a"));
      assertTrue(scripted.output().contains("obrigatório."));
   }

   @Test
   void devePedirNovamenteUmValorInvalido() {
      ScriptedConsole scripted = new ScriptedConsole("abc","5");
      Map<String, Object> values =
           new ArgumentPrompter(scripted.console()).readArguments(
                schema(Map.of("n",Map.of("type","integer")),"n"));

      assertEquals(5L,values.get("n"));
      assertTrue(scripted.output().contains("Valor inválido"));
   }

   @Test
   void deveExibirDescricaoEValoresPermitidos() {
      ScriptedConsole scripted = new ScriptedConsole("json");

      new ArgumentPrompter(scripted.console()).readArguments(
           schema(Map.of("f",Map.of("type","string","description","Formato",
                                    "enum",List.of("texto","json")))));

      assertTrue(scripted.output().contains("Descrição: Formato"));
      assertTrue(scripted.output().contains("[texto, json]"));
   }

   @Test
   void deveLerListaDeInteiros() {
      Map<String, Object> values = read(schema(
           Map.of("ids",Map.of("type","array",
                               "items",Map.of("type","integer")))),"1,2");

      assertEquals(List.of(1L,2L),values.get("ids"));
   }

   @Test
   void deveExplicarOFormatoDasListas() {
      ScriptedConsole scripted = new ScriptedConsole("[]");

      new ArgumentPrompter(scripted.console()).readArguments(
           schema(Map.of("ids",Map.of("type","array"))));

      assertTrue(scripted.output().contains("[] para vazia"));
   }

   @Test
   void deveGuiarOPreenchimentoDeListaDeObjetos() {
      Map<String, Object> item = schema(new LinkedHashMap<>(Map.of(
              "valor",Map.of("type","number"))),"valor");

      Map<String, Object> values = read(schema(
           Map.of("notas",Map.of("type","array","items",item)),"notas"),
           "sim","7,5","s","8","não");

      assertEquals(List.of(Map.of("valor",7.5),Map.of("valor",8.0)),
                   values.get("notas"));
   }

   @Test
   void deveAceitarListaDeObjetosSemItens() {
      Map<String, Object> item = schema(Map.of("a",Map.of("type","string")));

      assertEquals(List.of(),read(schema(
           Map.of("itens",Map.of("type","array","items",item))),"não")
                                                  .get("itens"));
   }

   @Test
   void deveLerObjetoAninhadoCampoACampo() {
      Map<String, Object> endereco = schema(
           Map.of("cidade",Map.of("type","string")),"cidade");

      assertEquals(Map.of("endereco",Map.of("cidade","Curitiba")),
                   read(schema(Map.of("endereco",endereco)),"Curitiba"));
   }

   @Test
   void deveLerObjetoSemPropriedadesEmJson() {
      assertEquals(Map.of("dados",Map.of("x",1)),
                   read(schema(Map.of("dados",Map.of("type","object"))),
                        "{\"x\":1}"));
   }

   @Test
   void deveLerTipoDesconhecidoEmJson() {
      assertEquals(Map.of("valor",List.of(1,"a")),
                   read(schema(Map.of("valor",Map.of("anyOf",List.of()))),
                        "[1,\"a\"]"));
   }

   @Test
   void deveEncerrarSeAEntradaTerminar() {
      ScriptedConsole scripted = new ScriptedConsole();

      assertEquals(Map.of(),new ArgumentPrompter(scripted.console())
              .readArguments(schema(Map.of("a",Map.of("type","string")),"a")));
   }
}
