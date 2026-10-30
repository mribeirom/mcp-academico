// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de lançamentos e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.LancamentoService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que registra a chamada (presenças e faltas) de uma aula
public class RegistrarFrequenciaTool extends AbstractTool {
   // serviço que faz o registro de verdade
   private final LancamentoService lancamentos;

   // recebe o serviço de lançamentos
   public RegistrarFrequenciaTool(LancamentoService lancamentos) {
      // guarda o serviço para usar ao executar
      this.lancamentos = lancamentos;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "registrar_frequencia";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Registrar frequência";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Registra a frequência de uma aula: os alunos não informados " +
             "como ausentes recebem presença. Repetir a data substitui o " +
             "registro anterior";
   }

   // parâmetros obrigatórios: disciplina, semestre, data e lista de ausentes
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required(Parametros.DISCIPLINA_ID,
                                        Parametros.disciplinaId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .required("data",JsonSchemaBuilder.string(
                                        "Data da aula no formato AAAA-MM-DD"))
                              .required("ausentes",JsonSchemaBuilder.arrayOf(
                                        "IDs dos alunos ausentes",
                                        JsonSchemaBuilder.integer(
                                                         "ID do aluno ausente")))
                              .build();
   }

   // esta ferramenta grava dados novos
   @Override
   protected ToolAnnotations annotations() {
      return WRITE;
   }

   // executa o registro
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê os parâmetros, registra a chamada e guarda quantos alunos tiveram
      // a presença registrada
      int registrados = lancamentos.registrarFrequencia(
                        arguments.requiredInt(Parametros.DISCIPLINA_ID),
                        arguments.requiredString(Parametros.SEMESTRE),
                        arguments.requiredString("data"),
                        arguments.requiredIntList("ausentes"));

      // devolve a quantidade de alunos registrados
      return ToolResults.json(Map.of("alunos_registrados",registrados));
   }
}
