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

// ferramenta que monta o ranking da turma pela média
public class RankingTurmaTool extends AbstractTool {
   // tamanho do ranking quando o usuário não informa
   private static final int LIMITE_PADRAO = 10;

   // serviço que monta o ranking de verdade
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public RankingTurmaTool(RelatorioService relatorios) {
      // guarda o serviço para usar ao executar
      this.relatorios = relatorios;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "ranking_turma";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Ranking da turma";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Classifica os alunos de uma turma pela média, da maior para a " +
             "menor. Alunos sem notas não são classificados";
   }

   // parâmetros: disciplina e semestre (obrigatórios) e limite (opcional)
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required(Parametros.DISCIPLINA_ID,
                                        Parametros.disciplinaId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .optional(Parametros.LIMITE,JsonSchemaBuilder.integer(
                                        "Tamanho do ranking, de 1 a 100 " +
                                        "(padrão: 10)"))
                              .build();
   }

   // esta ferramenta só lê dados
   @Override
   protected ToolAnnotations annotations() {
      return READ_ONLY;
   }

   // monta o ranking
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê o limite; se não foi informado, usa o padrão (10)
      int limite = arguments.optionalInt(Parametros.LIMITE)
                            .orElse(LIMITE_PADRAO);

      // monta o ranking e devolve em JSON
      return ToolResults.json(Map.of("ranking",relatorios.ranking(
                        arguments.requiredInt(Parametros.DISCIPLINA_ID),
                        arguments.requiredString(Parametros.SEMESTRE),limite)));
   }
}
