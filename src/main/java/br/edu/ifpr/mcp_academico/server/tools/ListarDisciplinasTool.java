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

// ferramenta que lista as disciplinas cadastradas
public class ListarDisciplinasTool extends AbstractTool {
   // serviço que faz a listagem de verdade
   private final DisciplinaService disciplinas;

   // recebe o serviço de disciplinas
   public ListarDisciplinasTool(DisciplinaService disciplinas) {
      // guarda o serviço para usar ao executar
      this.disciplinas = disciplinas;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "listar_disciplinas";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Listar disciplinas";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Lista as disciplinas cadastradas em ordem alfabética";
   }

   // parâmetro opcional: listar somente as disciplinas ativas
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .optional("somente_ativas",JsonSchemaBuilder.bool(
                                        "Exclui as disciplinas inativas " +
                                        "(padrão: false)"))
                              .build();
   }

   // esta ferramenta só lê dados
   @Override
   protected ToolAnnotations annotations() {
      return READ_ONLY;
   }

   // executa a listagem
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê o parâmetro; se não foi informado, lista todas (false)
      boolean somenteAtivas = arguments.optionalBoolean("somente_ativas")
                                       .orElse(false);

      // busca as disciplinas e devolve a lista em JSON
      return ToolResults.json(Map.of("disciplinas",
                                     disciplinas.listar(somenteAtivas)));
   }
}
