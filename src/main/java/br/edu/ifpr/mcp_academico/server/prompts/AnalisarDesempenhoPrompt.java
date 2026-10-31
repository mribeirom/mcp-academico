// pacote (pasta) dos prompts do servidor MCP
package br.edu.ifpr.mcp_academico.server.prompts;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa o serviço de relatórios e as classes de apoio do servidor
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.support.AbstractPrompt;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
// importa a classe da biblioteca MCP que descreve um argumento de prompt
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

// prompt que pede à IA uma análise do boletim de um aluno. Um prompt é um
// modelo de pergunta pronto, já preenchido com os dados do sistema
public class AnalisarDesempenhoPrompt extends AbstractPrompt {
   // nome técnico do prompt
   public static final String NAME = "analisar_desempenho";

   // serviço que monta o boletim
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public AnalisarDesempenhoPrompt(RelatorioService relatorios) {
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
      return "Analisar desempenho";
   }

   // descrição do prompt
   @Override
   protected String description() {
      return "Análise do boletim de um aluno em um semestre";
   }

   // argumentos obrigatórios: o aluno e o semestre
   @Override
   protected List<PromptArgument> arguments() {
      return List.of(required("aluno_id","ID do aluno"),
                     required("semestre","Semestre no formato AAAA/S"));
   }

   // monta o texto do prompt
   @Override
   protected String render(ArgumentReader arguments) {
      // busca o boletim do aluno e converte para JSON
      String boletim = JsonSupport.toJson(relatorios.boletim(
                                       arguments.requiredInt("aluno_id"),
                                       arguments.requiredString("semestre")));

      // texto com as instruções para a IA; o %s é trocado pelo boletim
      return """
             Você é um coordenador pedagógico. Analise o boletim a seguir e \
             descreva os pontos fortes do aluno, suas dificuldades e as \
             disciplinas que exigem atenção. Baseie-se somente nos dados \
             informados.

             Boletim:
             %s
             """.formatted(boletim);
   }
}
