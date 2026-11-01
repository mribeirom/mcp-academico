// pacote (pasta) do servidor MCP
package br.edu.ifpr.mcp_academico.server;

// importa as classes do Java para caminhos, relógio e listas
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

// importa os serviços (regras de negócio)
import br.edu.ifpr.mcp_academico.domain.service.AlunoService;
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.domain.service.DisciplinaService;
import br.edu.ifpr.mcp_academico.domain.service.LancamentoService;
import br.edu.ifpr.mcp_academico.domain.service.MatriculaService;
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
// importa os contratos dos repositórios (acesso ao banco)
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;
import br.edu.ifpr.mcp_academico.persistence.AvaliacaoRepository;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.FrequenciaRepository;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;
// importa as classes que acessam o banco SQLite de verdade
import br.edu.ifpr.mcp_academico.persistence.jdbc.Database;
import br.edu.ifpr.mcp_academico.persistence.jdbc.DatabaseBootstrap;
import br.edu.ifpr.mcp_academico.persistence.jdbc.DatabaseLocation;
import br.edu.ifpr.mcp_academico.persistence.jdbc.JdbcAlunoRepository;
import br.edu.ifpr.mcp_academico.persistence.jdbc.JdbcAvaliacaoRepository;
import br.edu.ifpr.mcp_academico.persistence.jdbc.JdbcDisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.jdbc.JdbcFrequenciaRepository;
import br.edu.ifpr.mcp_academico.persistence.jdbc.JdbcMatriculaRepository;
// importa os prompts
import br.edu.ifpr.mcp_academico.server.prompts.AnalisarDesempenhoPrompt;
import br.edu.ifpr.mcp_academico.server.prompts.PlanoRecuperacaoPrompt;
import br.edu.ifpr.mcp_academico.server.prompts.RelatorioTurmaPrompt;
// importa os resources
import br.edu.ifpr.mcp_academico.server.resources.DisciplinasResource;
import br.edu.ifpr.mcp_academico.server.resources.HistoricoAlunoResource;
import br.edu.ifpr.mcp_academico.server.resources.RegrasAprovacaoResource;
// importa os contratos das ferramentas, resources e prompts
import br.edu.ifpr.mcp_academico.server.support.McpPromptDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpResourceDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpResourceTemplateDefinition;
import br.edu.ifpr.mcp_academico.server.support.McpToolDefinition;
// importa as doze ferramentas
import br.edu.ifpr.mcp_academico.server.tools.BuscarAlunosTool;
import br.edu.ifpr.mcp_academico.server.tools.CadastrarAlunoTool;
import br.edu.ifpr.mcp_academico.server.tools.CadastrarDisciplinaTool;
import br.edu.ifpr.mcp_academico.server.tools.CalcularSituacaoTool;
import br.edu.ifpr.mcp_academico.server.tools.CancelarMatriculaTool;
import br.edu.ifpr.mcp_academico.server.tools.EstatisticasDisciplinaTool;
import br.edu.ifpr.mcp_academico.server.tools.GerarBoletimTool;
import br.edu.ifpr.mcp_academico.server.tools.LancarNotasTool;
import br.edu.ifpr.mcp_academico.server.tools.ListarDisciplinasTool;
import br.edu.ifpr.mcp_academico.server.tools.MatricularAlunoTool;
import br.edu.ifpr.mcp_academico.server.tools.RankingTurmaTool;
import br.edu.ifpr.mcp_academico.server.tools.RegistrarFrequenciaTool;

// recursos disponibilizados pelo servidor MCP: as listas de ferramentas,
// resources, templates de resources e prompts
public record ServerFeatures(
                     List<McpToolDefinition>             tools,
                     List<McpResourceDefinition>         resources,
                     List<McpResourceTemplateDefinition> resourceTemplates,
                     List<McpPromptDefinition>           prompts) {

   // ponto único de criação dos objetos da aplicação, que recebem suas
   // dependências pelo construtor: incluir uma ferramenta, um resource ou um
   // prompt não exige alteração em nenhuma outra classe
   public static ServerFeatures assemble() {
      // descobre onde fica o banco (variável de ambiente ou pasta do
      // usuário) e monta tudo com esse arquivo
      return assemble(DatabaseLocation.resolve(System.getenv(),
                                               System.getProperty("user.home")));
   }

   // monta todas as peças da aplicação usando o arquivo de banco informado
   public static ServerFeatures assemble(Path databaseFile) {
      // abre o banco (e cria as tabelas e os dados de exemplo, se preciso)
      Database             database    = DatabaseBootstrap.open(databaseFile);
      // cria um repositório para cada tabela principal
      AlunoRepository      alunos      = new JdbcAlunoRepository(database);
      DisciplinaRepository disciplinas = new JdbcDisciplinaRepository(database);
      MatriculaRepository  matriculas  = new JdbcMatriculaRepository(database);
      AvaliacaoRepository  avaliacoes  = new JdbcAvaliacaoRepository(database);
      FrequenciaRepository frequencias = new JdbcFrequenciaRepository(database);

      // cria os serviços, entregando a cada um os repositórios que ele usa
      AlunoService      alunoService      = new AlunoService(alunos);
      DisciplinaService disciplinaService = new DisciplinaService(disciplinas);
      MatriculaService  matriculaService  =
                    new MatriculaService(alunos,disciplinas,matriculas);
      // o relógio do sistema informa a data de hoje
      LancamentoService lancamentoService =
                    new LancamentoService(disciplinas,matriculas,avaliacoes,
                                          frequencias,
                                          Clock.systemDefaultZone());
      RelatorioService  relatorioService  =
                    new RelatorioService(alunos,disciplinas,matriculas,
                                         avaliacoes,frequencias,
                                         new CalculadoraSituacao());

      // junta tudo: as ferramentas, os resources, os templates e os prompts,
      // cada um recebendo o serviço de que precisa
      return new ServerFeatures(
                  List.of(new CadastrarAlunoTool(alunoService),
                          new BuscarAlunosTool(alunoService),
                          new CadastrarDisciplinaTool(disciplinaService),
                          new ListarDisciplinasTool(disciplinaService),
                          new MatricularAlunoTool(matriculaService),
                          new CancelarMatriculaTool(matriculaService),
                          new LancarNotasTool(lancamentoService),
                          new RegistrarFrequenciaTool(lancamentoService),
                          new CalcularSituacaoTool(relatorioService),
                          new GerarBoletimTool(relatorioService),
                          new RankingTurmaTool(relatorioService),
                          new EstatisticasDisciplinaTool(relatorioService)),
                  List.of(new DisciplinasResource(disciplinaService),
                          new RegrasAprovacaoResource()),
                  List.of(new HistoricoAlunoResource(relatorioService)),
                  List.of(new AnalisarDesempenhoPrompt(relatorioService),
                          new PlanoRecuperacaoPrompt(relatorioService),
                          new RelatorioTurmaPrompt(relatorioService)));
   }
}
