// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de disciplinas e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.DisciplinaService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que cadastra uma disciplina nova
public class CadastrarDisciplinaTool extends AbstractTool {
   // serviço que faz o cadastro de verdade
   private final DisciplinaService disciplinas;

   // recebe o serviço de disciplinas
   public CadastrarDisciplinaTool(DisciplinaService disciplinas) {
      // guarda o serviço para usar ao executar
      this.disciplinas = disciplinas;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "cadastrar_disciplina";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Cadastrar disciplina";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Cadastra uma nova disciplina, com código único";
   }

   // parâmetros obrigatórios: código, nome e carga horária
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required("codigo",JsonSchemaBuilder.string(
                                        "Código da disciplina, como SWEB"))
                              .required("nome",JsonSchemaBuilder.string(
                                        "Nome da disciplina"))
                              .required("carga_horaria",JsonSchemaBuilder.integer(
                                        "Carga horária, de 1 a 1000 horas"))
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
      // lê os parâmetros, cadastra a disciplina e devolve ela em JSON
      return ToolResults.json(disciplinas.cadastrar(
                                       arguments.requiredString("codigo"),
                                       arguments.requiredString("nome"),
                                       arguments.requiredInt("carga_horaria")));
   }
}
