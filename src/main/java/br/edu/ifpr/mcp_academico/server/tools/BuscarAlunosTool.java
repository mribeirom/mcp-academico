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

// ferramenta que busca alunos pelo nome, RA ou e-mail
public class BuscarAlunosTool extends AbstractTool {
   // serviço que faz a busca de verdade
   private final AlunoService alunos;

   // recebe o serviço de alunos
   public BuscarAlunosTool(AlunoService alunos) {
      // guarda o serviço para usar ao executar
      this.alunos = alunos;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "buscar_alunos";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Buscar alunos";
   }

   // descrição lida pela IA para decidir quando usar a ferramenta
   @Override
   protected String description() {
      return "Busca alunos por nome, RA ou e-mail, ou lista todos os alunos " +
             "quando nenhum termo é informado";
   }

   // parâmetros aceitos: o termo e o limite, os dois opcionais
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .optional("termo",JsonSchemaBuilder.string(
                                        "Trecho do nome, do RA ou do e-mail"))
                              .optional(Parametros.LIMITE,JsonSchemaBuilder.integer(
                                        "Máximo de alunos, de 1 a 100 " +
                                        "(padrão: 20)"))
                              .build();
   }

   // esta ferramenta só lê dados
   @Override
   protected ToolAnnotations annotations() {
      return READ_ONLY;
   }

   // executa a busca
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // lê o termo; se não foi informado, usa texto vazio (traz todos)
      String termo  = arguments.optionalString("termo").orElse("");
      // lê o limite; se não foi informado, usa o padrão (20)
      int    limite = arguments.optionalInt(Parametros.LIMITE)
                               .orElse(AlunoService.LIMITE_PADRAO);

      // busca os alunos e devolve a lista em JSON
      return ToolResults.json(Map.of("alunos",alunos.buscar(termo,limite)));
   }
}
