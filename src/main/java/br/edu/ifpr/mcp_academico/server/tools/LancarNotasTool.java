// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o tipo List (lista) e o Map (mapa que liga uma chave a um valor)
import java.util.List;
import java.util.Map;

// importa a nota do aluno, o serviço de lançamentos e as classes de apoio
import br.edu.ifpr.mcp_academico.domain.model.NotaAluno;
import br.edu.ifpr.mcp_academico.domain.service.LancamentoService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que lança as notas de uma avaliação para a turma
public class LancarNotasTool extends AbstractTool {
   // serviço que faz o lançamento de verdade
   private final LancamentoService lancamentos;

   // recebe o serviço de lançamentos
   public LancarNotasTool(LancamentoService lancamentos) {
      // guarda o serviço para usar ao executar
      this.lancamentos = lancamentos;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "lancar_notas";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Lançar notas";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Lança as notas de uma avaliação de uma turma. Se alguma nota " +
             "for inválida, nenhuma nota é gravada";
   }

   // parâmetros: disciplina, semestre, avaliação, peso e a lista de notas
   @Override
   protected Map<String, Object> inputSchema() {
      // esquema de cada item da lista de notas: o aluno e o valor da nota
      Map<String, Object> nota =
            JsonSchemaBuilder.object()
                             .required(Parametros.ALUNO_ID,Parametros.alunoId())
                             .required("valor",JsonSchemaBuilder.number(
                                                        "Nota, de 0 a 10"))
                             .build();

      // esquema completo da ferramenta
      return JsonSchemaBuilder.object()
                              .required(Parametros.DISCIPLINA_ID,
                                        Parametros.disciplinaId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .required("avaliacao",JsonSchemaBuilder.string(
                                        "Nome da avaliação, como Prova 1"))
                              .required("peso",JsonSchemaBuilder.number(
                                        "Peso da avaliação, maior que zero"))
                              .required("notas",JsonSchemaBuilder.arrayOf(
                                        "Nota de cada aluno",nota))
                              .build();
   }

   // esta ferramenta grava dados novos
   @Override
   protected ToolAnnotations annotations() {
      return WRITE;
   }

   // executa o lançamento
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // transforma cada item da lista recebida em uma NotaAluno
      List<NotaAluno> notas = arguments.requiredObjectList("notas")
                                       .stream()
                                       .map(this::toNota)
                                       .toList();
      // lança as notas e guarda quantas foram gravadas
      int lancadas = lancamentos.lancarNotas(
                        arguments.requiredInt(Parametros.DISCIPLINA_ID),
                        arguments.requiredString(Parametros.SEMESTRE),
                        arguments.requiredString("avaliacao"),
                        arguments.requiredDouble("peso"),notas);

      // devolve a quantidade de notas lançadas
      return ToolResults.json(Map.of("notas_lancadas",lancadas));
   }

   // converte um item da lista (aluno_id e valor) em NotaAluno
   private NotaAluno toNota(ArgumentReader item) {
      return new NotaAluno(item.requiredInt(Parametros.ALUNO_ID),
                           item.requiredDouble("valor"));
   }
}
