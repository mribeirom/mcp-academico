package br.edu.ifpr.mcp_academico.server.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.service.AlunoService;
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.domain.service.DisciplinaService;
import br.edu.ifpr.mcp_academico.domain.service.InMemoryRepositories;
import br.edu.ifpr.mcp_academico.domain.service.LancamentoService;
import br.edu.ifpr.mcp_academico.domain.service.MatriculaService;
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

// verifica a tradução de argumentos e resultados de cada ferramenta: as regras
// de negócio são testadas nas classes de teste dos serviços
class ToolsTest {
   private static final String SEMESTRE = "2026/1";

   private final InMemoryRepositories.Alunos      alunos      =
                                            new InMemoryRepositories.Alunos();
   private final InMemoryRepositories.Disciplinas disciplinas =
                                       new InMemoryRepositories.Disciplinas();
   private final InMemoryRepositories.Matriculas  matriculas  =
                                        new InMemoryRepositories.Matriculas();
   private final InMemoryRepositories.Avaliacoes  avaliacoes  =
                                        new InMemoryRepositories.Avaliacoes();
   private final InMemoryRepositories.Frequencias frequencias =
                                       new InMemoryRepositories.Frequencias();

   private final AlunoService      alunoService      =
                                   new AlunoService(alunos);
   private final DisciplinaService disciplinaService =
                                   new DisciplinaService(disciplinas);
   private final MatriculaService  matriculaService  =
                   new MatriculaService(alunos,disciplinas,matriculas);
   private final LancamentoService lancamentoService =
                   new LancamentoService(disciplinas,matriculas,avaliacoes,
                                         frequencias,
                                         Clock.fixed(Instant.parse(
                                                     "2026-10-31T12:00:00Z"),
                                                     ZoneOffset.UTC));
   private final RelatorioService  relatorioService  =
                   new RelatorioService(alunos,disciplinas,matriculas,
                                        avaliacoes,frequencias,
                                        new CalculadoraSituacao());

   @BeforeEach
   void prepararTurma() {
      alunos.save("1","Ana Conceição","ana@example.com");
      alunos.save("2","Bruno Simões","bruno@example.com");
      disciplinas.save("SWEB","Serviços Web",80);
      disciplinas.save("LEG","Legislação",40);
      disciplinas.inativar(2);
      matriculas.save(1,1,SEMESTRE);
      matriculas.save(2,1,SEMESTRE);
      avaliacoes.saveWithNotas(1,SEMESTRE,"Prova 1",1,
                               List.of(new NotaMatricula(1,9.0),
                                       new NotaMatricula(2,5.0)));
      frequencias.save(LocalDate.of(2026,3,2),Map.of(1,true,2,true));
   }

   private static String textOf(CallToolResult result) {
      return ((TextContent) result.content().get(0)).text();
   }

   @SuppressWarnings("unchecked")
   private static Map<String, Object> dataOf(CallToolResult result) {
      assertFalse(result.isError(),() -> textOf(result));

      return (Map<String, Object>) result.structuredContent();
   }

   private static void assertFailure(String code, CallToolResult result) {
      assertTrue(result.isError());
      assertTrue(textOf(result).startsWith("[" + code + "]"),textOf(result));
   }

   // ========================= cadastrar_aluno ============================

   @Test
   void cadastrarAlunoDeveRetornarOAlunoCriado() {
      Map<String, Object> data = dataOf(new CadastrarAlunoTool(alunoService)
              .call(Map.of("nome","Caio","email","caio@example.com",
                           "ra","3")));

      assertEquals(3,data.get("id"));
      assertEquals("Caio",data.get("nome"));
   }

   @Test
   void cadastrarAlunoDeveInformarErroDoServico() {
      assertFailure("EMAIL_INVALIDO",new CadastrarAlunoTool(alunoService)
              .call(Map.of("nome","Caio","email","caio","ra","3")));
   }

   @Test
   void cadastrarAlunoDeveExigirOsArgumentos() {
      assertFailure("ARGUMENTO_INVALIDO",new CadastrarAlunoTool(alunoService)
              .call(Map.of("nome","Caio")));
   }

   // ========================== buscar_alunos =============================

   @Test
   void buscarAlunosDeveUsarValoresPadrao() {
      List<?> encontrados = (List<?>) dataOf(new BuscarAlunosTool(alunoService)
              .call(Map.of())).get("alunos");

      assertEquals(2,encontrados.size());
   }

   @Test
   void buscarAlunosDeveFiltrarPeloTermoELimite() {
      List<?> encontrados = (List<?>) dataOf(new BuscarAlunosTool(alunoService)
              .call(Map.of("termo","Bruno","limite",1))).get("alunos");

      assertEquals(1,encontrados.size());
   }

   @Test
   void buscarAlunosDeveInformarLimiteInvalido() {
      assertFailure("LIMITE_INVALIDO",new BuscarAlunosTool(alunoService)
              .call(Map.of("limite",0)));
   }

   // ======================= cadastrar_disciplina =========================

   @Test
   void cadastrarDisciplinaDeveRetornarADisciplinaCriada() {
      Map<String, Object> data = dataOf(
              new CadastrarDisciplinaTool(disciplinaService)
                 .call(Map.of("codigo","bd","nome","Banco de Dados",
                              "carga_horaria",80)));

      assertEquals("BD",data.get("codigo"));
      assertEquals(true,data.get("ativa"));
   }

   @Test
   void cadastrarDisciplinaDeveInformarCodigoDuplicado() {
      assertFailure("DISCIPLINA_DUPLICADA",
              new CadastrarDisciplinaTool(disciplinaService)
                 .call(Map.of("codigo","SWEB","nome","Web",
                              "carga_horaria",80)));
   }

   // ======================== listar_disciplinas ==========================

   @Test
   void listarDisciplinasDeveRetornarTodasPorPadrao() {
      assertEquals(2,((List<?>) dataOf(
              new ListarDisciplinasTool(disciplinaService).call(Map.of()))
                 .get("disciplinas")).size());
   }

   @Test
   void listarDisciplinasDeveFiltrarAsAtivas() {
      assertEquals(1,((List<?>) dataOf(
              new ListarDisciplinasTool(disciplinaService)
                 .call(Map.of("somente_ativas",true)))
                 .get("disciplinas")).size());
   }

   // ========================= matricular_aluno ===========================

   @Test
   void matricularAlunoDeveRetornarAMatricula() {
      Map<String, Object> data = dataOf(new MatricularAlunoTool(matriculaService)
              .call(Map.of("aluno_id",1,"disciplina_id",1,
                           "semestre","2026/2")));

      assertEquals("ATIVA",data.get("status"));
      assertEquals("2026/2",data.get("semestre"));
   }

   @Test
   void matricularAlunoDeveInformarDisciplinaInativa() {
      assertFailure("DISCIPLINA_INATIVA",new MatricularAlunoTool(matriculaService)
              .call(Map.of("aluno_id",1,"disciplina_id",2,
                           "semestre","2026/2")));
   }

   // ======================== cancelar_matricula ==========================

   @Test
   void cancelarMatriculaDeveRetornarAMatriculaCancelada() {
      assertEquals("CANCELADA",dataOf(
              new CancelarMatriculaTool(matriculaService)
                 .call(Map.of("matricula_id",1))).get("status"));
   }

   @Test
   void cancelarMatriculaDeveInformarMatriculaInexistente() {
      assertFailure("MATRICULA_NAO_ENCONTRADA",
              new CancelarMatriculaTool(matriculaService)
                 .call(Map.of("matricula_id",99)));
   }

   // =========================== lancar_notas =============================

   @Test
   void lancarNotasDeveConverterAListaDeObjetos() {
      Map<String, Object> data = dataOf(new LancarNotasTool(lancamentoService)
              .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,
                           "avaliacao","Prova 2","peso",2.5,
                           "notas",List.of(Map.of("aluno_id",1,"valor",8),
                                           Map.of("aluno_id",2,"valor",7.5)))));

      assertEquals(2,data.get("notas_lancadas"));
      assertEquals(2,avaliacoes.findNotas(1).size());
   }

   @Test
   void lancarNotasDeveInformarNotaInvalida() {
      assertFailure("NOTA_FORA_DA_FAIXA",new LancarNotasTool(lancamentoService)
              .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,
                           "avaliacao","Prova 2","peso",1,
                           "notas",List.of(Map.of("aluno_id",1,"valor",11)))));
   }

   @Test
   void lancarNotasDeveExigirOsCamposDeCadaNota() {
      assertFailure("ARGUMENTO_INVALIDO",new LancarNotasTool(lancamentoService)
              .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,
                           "avaliacao","Prova 2","peso",1,
                           "notas",List.of(Map.of("aluno_id",1)))));
   }

   // ======================= registrar_frequencia =========================

   @Test
   void registrarFrequenciaDeveConverterAListaDeAusentes() {
      Map<String, Object> data = dataOf(
              new RegistrarFrequenciaTool(lancamentoService)
                 .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,
                              "data","2026-03-09","ausentes",List.of(2))));

      assertEquals(2,data.get("alunos_registrados"));
      assertEquals(1,frequencias.resumo(2).presencas());
      assertEquals(2,frequencias.resumo(1).presencas());
   }

   @Test
   void registrarFrequenciaDeveInformarDataInvalida() {
      assertFailure("DATA_INVALIDA",
              new RegistrarFrequenciaTool(lancamentoService)
                 .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,
                              "data","09/03/2026","ausentes",List.of())));
   }

   // ======================== calcular_situacao ===========================

   @Test
   void calcularSituacaoDeveRetornarOResultado() {
      Map<String, Object> data = dataOf(new CalcularSituacaoTool(relatorioService)
              .call(Map.of("aluno_id",1,"disciplina_id",1,
                           "semestre",SEMESTRE)));

      assertEquals(9.0,data.get("media"));
      assertEquals(100.0,data.get("frequencia"));
      assertEquals("APROVADO",data.get("situacao"));
   }

   @Test
   void calcularSituacaoDeveInformarMatriculaInexistente() {
      assertFailure("MATRICULA_NAO_ENCONTRADA",
              new CalcularSituacaoTool(relatorioService)
                 .call(Map.of("aluno_id",1,"disciplina_id",1,
                              "semestre","2025/1")));
   }

   // ========================== gerar_boletim =============================

   @Test
   void gerarBoletimDeveFormatarComoTexto() {
      CallToolResult result = new GerarBoletimTool(relatorioService)
              .call(Map.of("aluno_id",1,"semestre",SEMESTRE,
                           "formato","texto"));

      assertTrue(textOf(result).contains("Boletim de Ana Conceição"));
      assertTrue(textOf(result).contains("Média: 9,00"));
      assertTrue(textOf(result).contains("Situação: APROVADO"));
   }

   @Test
   void gerarBoletimDeveFormatarComoJson() {
      CallToolResult result = new GerarBoletimTool(relatorioService)
              .call(Map.of("aluno_id",1,"semestre",SEMESTRE,
                           "formato","json"));

      assertTrue(textOf(result).contains("\"semestre\" : \"2026/1\""));
      assertEquals(SEMESTRE,dataOf(result).get("semestre"));
   }

   @Test
   void gerarBoletimDeveRejeitarFormatoInexistente() {
      assertFailure("ARGUMENTO_INVALIDO",new GerarBoletimTool(relatorioService)
              .call(Map.of("aluno_id",1,"semestre",SEMESTRE,
                           "formato","pdf")));
   }

   @Test
   void gerarBoletimDeveInformarAlunoSemMatriculas() {
      assertFailure("SEM_MATRICULAS",new GerarBoletimTool(relatorioService)
              .call(Map.of("aluno_id",1,"semestre","2025/1",
                           "formato","texto")));
   }

   // ========================== ranking_turma =============================

   @Test
   void rankingTurmaDeveUsarLimitePadrao() {
      assertEquals(2,((List<?>) dataOf(new RankingTurmaTool(relatorioService)
              .call(Map.of("disciplina_id",1,"semestre",SEMESTRE)))
                 .get("ranking")).size());
   }

   @Test
   void rankingTurmaDeveRespeitarOLimiteInformado() {
      assertEquals(1,((List<?>) dataOf(new RankingTurmaTool(relatorioService)
              .call(Map.of("disciplina_id",1,"semestre",SEMESTRE,"limite",1)))
                 .get("ranking")).size());
   }

   @Test
   void rankingTurmaDeveInformarTurmaVazia() {
      assertFailure("TURMA_VAZIA",new RankingTurmaTool(relatorioService)
              .call(Map.of("disciplina_id",1,"semestre","2025/1")));
   }

   // ===================== estatisticas_disciplina ========================

   @Test
   void estatisticasDisciplinaDeveRetornarOsIndicadores() {
      Map<String, Object> data = dataOf(
              new EstatisticasDisciplinaTool(relatorioService)
                 .call(Map.of("disciplina_id",1,"semestre",SEMESTRE)));

      assertEquals(2,data.get("alunos"));
      assertEquals(7.0,data.get("media"));
      assertEquals(50.0,data.get("taxa_aprovacao"));
   }

   @Test
   void estatisticasDisciplinaDeveInformarDisciplinaInexistente() {
      assertFailure("DISCIPLINA_NAO_ENCONTRADA",
              new EstatisticasDisciplinaTool(relatorioService)
                 .call(Map.of("disciplina_id",99,"semestre",SEMESTRE)));
   }
}
