// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de relatórios e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que calcula as estatísticas de uma turma
public class EstatisticasDisciplinaTool extends AbstractTool {
   // serviço que faz o cálculo de verdade
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public EstatisticasDisciplinaTool(RelatorioService relatorios) {
      // guarda o serviço para usar ao executar
      this.relatorios = relatorios;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "estatisticas_disciplina";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Estatísticas da disciplina";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Calcula as estatísticas de uma turma: média, maior e menor " +
             "média, desvio padrão, situações e taxa de aprovação";
   }

   // parâmetros obrigatórios: disciplina e semestre
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required(Parametros.DISCIPLINA_ID,
                                        Parametros.disciplinaId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .build();
   }

   // esta ferramenta só lê dados
   @Override
   protected ToolAnnotations annotations() {
      return READ_ONLY;
   }

   // executa o cálculo
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê os parâmetros, calcula as estatísticas e devolve em JSON
      return ToolResults.json(relatorios.estatisticas(
                        arguments.requiredInt(Parametros.DISCIPLINA_ID),
                        arguments.requiredString(Parametros.SEMESTRE)));
   }
}
