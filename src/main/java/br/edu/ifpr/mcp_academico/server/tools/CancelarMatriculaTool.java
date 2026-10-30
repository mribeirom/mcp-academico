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

// ferramenta que cancela uma matrícula
public class CancelarMatriculaTool extends AbstractTool {
   // serviço que faz o cancelamento de verdade
   private final MatriculaService matriculas;

   // recebe o serviço de matrículas
   public CancelarMatriculaTool(MatriculaService matriculas) {
      // guarda o serviço para usar ao executar
      this.matriculas = matriculas;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "cancelar_matricula";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Cancelar matrícula";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Cancela uma matrícula ativa, retirando o aluno da turma";
   }

   // parâmetro obrigatório: o ID da matrícula
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required("matricula_id",JsonSchemaBuilder.integer(
                                                           "ID da matrícula"))
                              .build();
   }

   // esta ferramenta desfaz algo que existia (a matrícula ativa)
   @Override
   protected ToolAnnotations annotations() {
      return DESTRUCTIVE;
   }

   // executa o cancelamento
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê o ID, cancela a matrícula e devolve a matrícula atualizada
      return ToolResults.json(matriculas.cancelar(
                                      arguments.requiredInt("matricula_id")));
   }
}
