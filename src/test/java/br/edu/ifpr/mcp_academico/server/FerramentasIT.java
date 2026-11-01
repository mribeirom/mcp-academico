package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

// simula a aplicação cliente executando cada ferramenta no servidor real, com
// os dados de exemplo criados na primeira execução
class FerramentasIT extends McpServerITSupport {
   private static CallToolResult call(String tool, Map<String, Object> args) {
      return client.callTool(CallToolRequest.builder(tool)
                                            .arguments(args)
                                            .build());
   }

   private static String textOf(CallToolResult result) {
      return ((TextContent) result.content().get(0)).text();
   }

   @SuppressWarnings("unchecked")
   private static Map<String, Object> success(String tool,
                                              Map<String, Object> args) {
      CallToolResult result = call(tool,args);

      assertFalse(result.isError(),() -> textOf(result));

      return (Map<String, Object>) result.structuredContent();
   }

   private static void assertFailure(String code, String tool,
                                     Map<String, Object> args) {
      CallToolResult result = call(tool,args);

      assertTrue(result.isError(),() -> textOf(result));
      assertTrue(textOf(result).contains(code),() -> textOf(result));
   }

   @Test
   void deveDisponibilizarAsDozeFerramentasComDescricaoEEsquema() {
      List<Tool> tools = client.listTools().tools();

      assertEquals(12,tools.size());
      tools.forEach(tool -> {
         assertFalse(tool.description().isBlank(),tool.name());
         assertFalse(tool.inputSchema().isEmpty(),tool.name());
         assertTrue(tool.annotations() != null,tool.name());
      });
   }

   @Test
   @SuppressWarnings("unchecked")
   void deveManterAOrdemDosParametrosNoEsquema() {
      Tool tool = client.listTools().tools().stream()
                        .filter(item -> item.name().equals("lancar_notas"))
                        .findFirst().orElseThrow();
      Map<String, Object> properties =
                     (Map<String, Object>) tool.inputSchema().get("properties");

      assertEquals(List.of("disciplina_id","semestre","avaliacao","peso",
                           "notas"),
                   new ArrayList<>(properties.keySet()));
   }

   @Test
   void cadastrarAluno() {
      Map<String, Object> aluno = success("cadastrar_aluno",
                  Map.of("nome","Íris Conceição",
                         "email","iris@example.com","ra","IT0001"));

      assertEquals("Íris Conceição",aluno.get("nome"));
      assertFailure("ALUNO_DUPLICADO","cadastrar_aluno",
                  Map.of("nome","Íris","email","outra@example.com",
                         "ra","IT0001"));
      assertFailure("EMAIL_INVALIDO","cadastrar_aluno",
                  Map.of("nome","Íris","email","invalido","ra","IT0002"));
   }

   @Test
   void buscarAlunos() {
      List<?> alunos = (List<?>) success("buscar_alunos",
                                  Map.of("termo","Conceição","limite",1))
                                 .get("alunos");

      assertEquals(1,alunos.size());
      assertFailure("LIMITE_INVALIDO","buscar_alunos",Map.of("limite",0));
   }

   @Test
   void cadastrarDisciplina() {
      assertEquals("IT1",success("cadastrar_disciplina",
                  Map.of("codigo","it1","nome","Testes de Integração",
                         "carga_horaria",40)).get("codigo"));
      assertFailure("DISCIPLINA_DUPLICADA","cadastrar_disciplina",
                  Map.of("codigo","SWEB","nome","Web","carga_horaria",40));
      assertFailure("CARGA_INVALIDA","cadastrar_disciplina",
                  Map.of("codigo","IT2","nome","Web","carga_horaria",0));
   }

   @Test
   void listarDisciplinas() {
      int todas  = ((List<?>) success("listar_disciplinas",Map.of())
                                .get("disciplinas")).size();
      int ativas = ((List<?>) success("listar_disciplinas",
                                      Map.of("somente_ativas",true))
                                .get("disciplinas")).size();

      assertTrue(ativas < todas,"A disciplina inativa deveria ser excluída.");
   }

   @Test
   void matricularECancelar() {
      Map<String, Object> matricula = success("matricular_aluno",
                  Map.of("aluno_id",2,"disciplina_id",3,"semestre","2026/2"));
      int                 id        = (Integer) matricula.get("id");

      assertEquals("ATIVA",matricula.get("status"));
      assertFailure("MATRICULA_DUPLICADA","matricular_aluno",
                  Map.of("aluno_id",2,"disciplina_id",3,"semestre","2026/2"));
      assertFailure("DISCIPLINA_INATIVA","matricular_aluno",
                  Map.of("aluno_id",2,"disciplina_id",4,"semestre","2026/2"));
      assertFailure("SEMESTRE_INVALIDO","matricular_aluno",
                  Map.of("aluno_id",2,"disciplina_id",3,"semestre","2026"));
      assertFailure("ALUNO_NAO_ENCONTRADO","matricular_aluno",
                  Map.of("aluno_id",999,"disciplina_id",3,
                         "semestre","2026/2"));

      assertEquals("CANCELADA",success("cancelar_matricula",
                  Map.of("matricula_id",id)).get("status"));
      assertFailure("MATRICULA_JA_CANCELADA","cancelar_matricula",
                  Map.of("matricula_id",id));
      assertFailure("MATRICULA_NAO_ENCONTRADA","cancelar_matricula",
                  Map.of("matricula_id",999));
   }

   @Test
   void lancarNotasComArrayDeObjetos() {
      assertEquals(2,success("lancar_notas",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "avaliacao","Prova IT","peso",5.0,
                         "notas",List.of(Map.of("aluno_id",7,"valor",7.5),
                                         Map.of("aluno_id",8,"valor",6)))
                  ).get("notas_lancadas"));
      assertFailure("AVALIACAO_DUPLICADA","lancar_notas",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "avaliacao","Prova IT","peso",5.0,
                         "notas",List.of(Map.of("aluno_id",7,"valor",7.5))));
   }

   @Test
   void lancarNotasDeveSerTudoOuNada() {
      assertFailure("ALUNO_NAO_MATRICULADO","lancar_notas",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "avaliacao","Prova Parcial","peso",1.0,
                         "notas",List.of(Map.of("aluno_id",7,"valor",7),
                                         Map.of("aluno_id",2,"valor",7))));
      assertEquals(1,success("lancar_notas",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "avaliacao","Prova Parcial","peso",1.0,
                         "notas",List.of(Map.of("aluno_id",7,"valor",7)))
                  ).get("notas_lancadas"));
   }

   @Test
   void registrarFrequenciaComArrayDeInteiros() {
      assertEquals(3,success("registrar_frequencia",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "data","2026-08-03","ausentes",List.of(8))
                  ).get("alunos_registrados"));
      assertFailure("DATA_INVALIDA","registrar_frequencia",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "data",LocalDate.now().plusDays(1).toString(),
                         "ausentes",List.of()));
      assertFailure("ALUNO_NAO_MATRICULADO","registrar_frequencia",
                  Map.of("disciplina_id",1,"semestre","2026/2",
                         "data","2026-08-03","ausentes",List.of(2)));
   }

   @Test
   void calcularSituacaoDeCadaCenarioDosDadosDeExemplo() {
      assertEquals("APROVADO",situacao(1));
      assertEquals("APROVADO",situacao(2));
      assertEquals("REPROVADO_NOTA",situacao(3));
      assertEquals("REPROVADO_FREQUENCIA",situacao(4));
      assertEquals("CANCELADA",situacao(6));
      assertFailure("MATRICULA_NAO_ENCONTRADA","calcular_situacao",
                  Map.of("aluno_id",8,"disciplina_id",1,"semestre","2026/1"));
   }

   private Object situacao(int alunoId) {
      return success("calcular_situacao",
                  Map.of("aluno_id",alunoId,"disciplina_id",1,
                         "semestre","2026/1")).get("situacao");
   }

   @Test
   void gerarBoletimNosDoisFormatos() {
      CallToolResult texto = call("gerar_boletim",
                  Map.of("aluno_id",1,"semestre","2026/1",
                         "formato","texto"));
      CallToolResult json  = call("gerar_boletim",
                  Map.of("aluno_id",1,"semestre","2026/1",
                         "formato","json"));

      assertTrue(textOf(texto).contains("Ana Luíza Gonçalves"),textOf(texto));
      assertTrue(textOf(texto).contains("Média: 8,60"),textOf(texto));
      assertTrue(textOf(json).contains("\"media\" : 8.6"),textOf(json));
      assertFailure("SEM_MATRICULAS","gerar_boletim",
                  Map.of("aluno_id",1,"semestre","2020/1",
                         "formato","texto"));
   }

   @Test
   void deveRejeitarFormatoForaDaEnumeracao() {
      assertFailure("formato","gerar_boletim",
                  Map.of("aluno_id",1,"semestre","2026/1","formato","pdf"));
   }

   @Test
   @SuppressWarnings("unchecked")
   void rankingTurma() {
      List<Map<String, Object>> ranking = (List<Map<String, Object>>)
                  success("ranking_turma",
                          Map.of("disciplina_id",1,"semestre","2026/1",
                                 "limite",2)).get("ranking");

      assertEquals(2,ranking.size());
      assertEquals("Luís Fernando Müller",ranking.get(0).get("nome"));
      assertEquals(1,ranking.get(0).get("posicao"));
      assertFailure("TURMA_VAZIA","ranking_turma",
                  Map.of("disciplina_id",1,"semestre","2020/1"));
   }

   @Test
   void estatisticasDisciplina() {
      Map<String, Object> dados = success("estatisticas_disciplina",
                  Map.of("disciplina_id",1,"semestre","2026/1"));

      assertEquals(5,dados.get("alunos"));
      assertEquals(3,dados.get("aprovados"));
      assertEquals(60.0,dados.get("taxa_aprovacao"));
      assertFailure("DISCIPLINA_NAO_ENCONTRADA","estatisticas_disciplina",
                  Map.of("disciplina_id",999,"semestre","2026/1"));
   }

   @Test
   void deveRejeitarArgumentoForaDoEsquema() {
      assertFailure("aluno_id","calcular_situacao",
                  Map.of("aluno_id","abc","disciplina_id",1,
                         "semestre","2026/1"));
   }
}
