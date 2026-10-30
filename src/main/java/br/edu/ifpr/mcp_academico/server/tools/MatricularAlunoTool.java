// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de matrículas e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.MatriculaService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que matricula um aluno em uma disciplina
public class MatricularAlunoTool extends AbstractTool {
   // serviço que faz a matrícula de verdade
   private final MatriculaService matriculas;

   // recebe o serviço de matrículas
   public MatricularAlunoTool(MatriculaService matriculas) {
      // guarda o serviço para usar ao executar
      this.matriculas = matriculas;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "matricular_aluno";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Matricular aluno";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Matricula um aluno em uma disciplina ativa, reativando a " +
             "matrícula caso ela tenha sido cancelada";
   }

   // parâmetros obrigatórios: aluno, disciplina e semestre
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required(Parametros.ALUNO_ID,
                                        Parametros.alunoId())
                              .required(Parametros.DISCIPLINA_ID,
                                        Parametros.disciplinaId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .build();
   }

   // esta ferramenta grava dados novos
   @Override
   protected ToolAnnotations annotations() {
      return WRITE;
   }

   // executa a matrícula
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê os parâmetros, matricula o aluno e devolve a matrícula em JSON
      return ToolResults.json(matriculas.matricular(
                        arguments.requiredInt(Parametros.ALUNO_ID),
                        arguments.requiredInt(Parametros.DISCIPLINA_ID),
                        arguments.requiredString(Parametros.SEMESTRE)));
   }
}
