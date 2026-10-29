package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;

class JsonSupportTest {
   @Test
   void deveUsarNomesEmSnakeCase() {
      Map<String, Object> map =
           JsonSupport.toMap(new Disciplina(1,"SWEB","Web",80,true));

      assertEquals(80,map.get("carga_horaria"));
   }

   @Test
   void deveManterValoresNulosEEnumeracoes() {
      Map<String, Object> expected = new HashMap<>();

      expected.put("media",null);
      expected.put("frequencia",75.0);
      expected.put("situacao","EM_CURSO");

      assertEquals(expected,JsonSupport.toMap(
           new ResultadoAcademico(null,75.0,Situacao.EM_CURSO)));
   }

   @Test
   void deveGerarJsonLegivelComAcentos() {
      String json = JsonSupport.toJson(Map.of("nome","Conceição"));

      assertTrue(json.contains("\"nome\" : \"Conceição\""),json);
   }
}
