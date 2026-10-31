package br.edu.ifpr.mcp_academico.server.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.ServerFixture;
import br.edu.ifpr.mcp_academico.server.support.AbstractPrompt;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class PromptsTest {
   private static final String SEMESTRE = ServerFixture.SEMESTRE;

   private final RelatorioService relatorios = new ServerFixture().relatorios;

   private final AnalisarDesempenhoPrompt desempenho =
                                  new AnalisarDesempenhoPrompt(relatorios);
   private final PlanoRecuperacaoPrompt   recuperacao =
                                  new PlanoRecuperacaoPrompt(relatorios);
   private final RelatorioTurmaPrompt     turma =
                                  new RelatorioTurmaPrompt(relatorios);

   private static String textOf(GetPromptResult result) {
      PromptMessage message = result.messages().get(0);

      assertEquals(Role.USER,message.role());

      return ((TextContent) message.content()).text();
   }

   private static void assertFailure(String text, AbstractPrompt prompt,
                                     Map<String, Object> arguments) {
      McpError error = assertThrows(McpError.class,
                                    () -> prompt.get(arguments));

      assertTrue(error.getMessage().contains(text),error.getMessage());
   }

   @Test
   void deveDeclararSomenteArgumentosObrigatorios() {
      Prompt prompt = recuperacao.specification().prompt();

      assertEquals(PlanoRecuperacaoPrompt.NAME,prompt.name());
      assertEquals(List.of("aluno_id","disciplina_id","semestre"),
                   prompt.arguments().stream().map(PromptArgument::name)
                         .toList());
      assertTrue(prompt.arguments().stream()
                       .allMatch(PromptArgument::required));
   }

   @Test
   void oHandlerDaEspecificacaoDeveGerarOPrompt() {
      GetPromptRequest request =
            GetPromptRequest.builder(AnalisarDesempenhoPrompt.NAME)
                            .arguments(Map.of("aluno_id","1",
                                              "semestre",SEMESTRE))
                            .build();

      assertTrue(textOf(desempenho.specification().promptHandler()
                                  .apply(null,request))
                                                 .contains("Ana Conceição"));
   }

   // =========================== analisar_desempenho ======================

   @Test
   void analisarDesempenhoDeveIncluirOBoletim() {
      String text = textOf(desempenho.get(Map.of("aluno_id","1",
                                                 "semestre",SEMESTRE)));

      assertTrue(text.contains("coordenador pedagógico"),text);
      assertTrue(text.contains("Ana Conceição"),text);
      assertTrue(text.contains("APROVADO"),text);
   }

   // o SDK não valida os argumentos obrigatórios de um prompt
   @Test
   void analisarDesempenhoDeveExigirOSemestre() {
      assertFailure("semestre",desempenho,Map.of("aluno_id","1"));
   }

   @Test
   void analisarDesempenhoDeveRejeitarIdNaoNumerico() {
      assertFailure("ARGUMENTO_INVALIDO",desempenho,
                    Map.of("aluno_id","abc","semestre",SEMESTRE));
   }

   @Test
   void analisarDesempenhoDeveInformarAlunoSemMatriculas() {
      assertFailure("SEM_MATRICULAS",desempenho,
                    Map.of("aluno_id","1","semestre","2020/1"));
   }

   // ============================ plano_recuperacao =======================

   @Test
   void planoRecuperacaoDeveIncluirSituacaoECriterios() {
      String text = textOf(recuperacao.get(
            Map.of("aluno_id","1","disciplina_id","1","semestre",SEMESTRE)));

      assertTrue(text.contains("quatro semanas"),text);
      assertTrue(text.contains("75%"),text);
      assertTrue(text.contains("\"media\" : 9.0"),text);
   }

   @Test
   void planoRecuperacaoDeveInformarMatriculaInexistente() {
      assertFailure("MATRICULA_NAO_ENCONTRADA",recuperacao,
            Map.of("aluno_id","1","disciplina_id","9","semestre",SEMESTRE));
   }

   // ============================= relatorio_turma ========================

   @Test
   void relatorioTurmaDeveIncluirEstatisticasERanking() {
      String text = textOf(turma.get(Map.of("disciplina_id","1",
                                            "semestre",SEMESTRE)));

      assertTrue(text.contains("coordenador de curso"),text);
      assertTrue(text.contains("taxa_aprovacao"),text);
      assertTrue(text.contains("Ana Conceição"),text);
   }

   @Test
   void relatorioTurmaDeveInformarTurmaVazia() {
      assertFailure("TURMA_VAZIA",turma,
                    Map.of("disciplina_id","1","semestre","2020/1"));
   }
}
