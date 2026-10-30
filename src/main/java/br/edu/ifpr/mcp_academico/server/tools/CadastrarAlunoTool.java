// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de alunos e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.AlunoService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que cadastra um aluno novo
public class CadastrarAlunoTool extends AbstractTool {
   // serviço que faz o cadastro de verdade
   private final AlunoService alunos;

   // recebe o serviço de alunos
   public CadastrarAlunoTool(AlunoService alunos) {
      // guarda o serviço para usar ao executar
      this.alunos = alunos;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "cadastrar_aluno";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Cadastrar aluno";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Cadastra um novo aluno, com RA e e-mail únicos";
   }

   // parâmetros obrigatórios: nome, e-mail e RA
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required("nome",JsonSchemaBuilder.string(
                                                     "Nome completo do aluno"))
                              .required("email",JsonSchemaBuilder.string(
                                                     "E-mail do aluno"))
                              .required("ra",JsonSchemaBuilder.string(
                                                     "Registro acadêmico (RA)"))
                              .build();
   }

   // esta ferramenta grava dados novos
   @Override
   protected ToolAnnotations annotations() {
      return WRITE;
   }

   // executa o cadastro
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê os três parâmetros, cadastra o aluno e devolve o aluno em JSON
      return ToolResults.json(alunos.cadastrar(
                                          arguments.requiredString("nome"),
                                          arguments.requiredString("email"),
                                          arguments.requiredString("ra")));
   }
}
