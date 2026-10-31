// pacote (pasta) dos prompts do servidor MCP
package br.edu.ifpr.mcp_academico.server.prompts;

// importa o tipo List (lista) e o Map (mapa que liga uma chave a um valor)
import java.util.List;
import java.util.Map;

// importa o serviço de relatórios e as classes de apoio do servidor
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.support.AbstractPrompt;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
// importa a classe da biblioteca MCP que descreve um argumento de prompt
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

// prompt que pede à IA um relatório sobre o desempenho de uma turma
public class RelatorioTurmaPrompt extends AbstractPrompt {
   // nome técnico do prompt
   public static final String NAME = "relatorio_turma";

   // quantidade máxima de alunos no ranking enviado à IA
   private static final int LIMITE_RANKING = 100;

   // serviço que calcula as estatísticas e o ranking
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public RelatorioTurmaPrompt(RelatorioService relatorios) {
      // guarda o serviço para usar ao gerar o texto
      this.relatorios = relatorios;
   }

   // nome técnico do prompt
   @Override
   protected String name() {
      return NAME;
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Relatório da turma";
   }

   // descrição do prompt
   @Override
   protected String description() {
      return "Relatório descritivo do desempenho de uma turma";
   }

   // argumentos obrigatórios: disciplina e semestre
   @Override
   protected List<PromptArgument> arguments() {
      return List.of(required("disciplina_id","ID da disciplina"),
                     required("semestre","Semestre no formato AAAA/S"));
   }

   // monta o texto do prompt
   @Override
   protected String render(ArgumentReader arguments) {
      // lê a disciplina e o semestre
      int    disciplinaId = arguments.requiredInt("disciplina_id");
      String semestre     = arguments.requiredString("semestre");
      // junta as estatísticas e o ranking da turma em um único JSON
      String dados        = JsonSupport.toJson(Map.of(
             "estatisticas",relatorios.estatisticas(disciplinaId,semestre),
             "ranking",relatorios.ranking(disciplinaId,semestre,
                                          LIMITE_RANKING)));

      // texto com as instruções para a IA; o %s é trocado pelos dados
      return """
             Você é um coordenador de curso. Escreva um relatório sobre o \
             desempenho da turma a seguir, destacando o resultado geral, a \
             distribuição das situações e os alunos que precisam de \
             acompanhamento. Baseie-se somente nos dados informados.

             Dados da turma:
             %s
             """.formatted(dados);
   }
}
