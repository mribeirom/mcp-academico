// pacote (pasta) dos prompts do servidor MCP
package br.edu.ifpr.mcp_academico.server.prompts;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa o resultado do aluno, a calculadora e o serviço de relatórios
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
// importa as classes de apoio do servidor
import br.edu.ifpr.mcp_academico.server.support.AbstractPrompt;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
// importa a classe da biblioteca MCP que descreve um argumento de prompt
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

// prompt que pede à IA um plano de recuperação para um aluno
public class PlanoRecuperacaoPrompt extends AbstractPrompt {
   // nome técnico do prompt
   public static final String NAME = "plano_recuperacao";

   // serviço que calcula a situação do aluno
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public PlanoRecuperacaoPrompt(RelatorioService relatorios) {
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
      return "Plano de recuperação";
   }

   // descrição do prompt
   @Override
   protected String description() {
      return "Plano de estudos para um aluno em uma disciplina";
   }

   // argumentos obrigatórios: aluno, disciplina e semestre
   @Override
   protected List<PromptArgument> arguments() {
      return List.of(required("aluno_id","ID do aluno"),
                     required("disciplina_id","ID da disciplina"),
                     required("semestre","Semestre no formato AAAA/S"));
   }

   // monta o texto do prompt
   @Override
   protected String render(ArgumentReader arguments) {
      // calcula a situação atual do aluno na disciplina
      ResultadoAcademico resultado = relatorios.situacao(
                                      arguments.requiredInt("aluno_id"),
                                      arguments.requiredInt("disciplina_id"),
                                      arguments.requiredString("semestre"));

      // texto com as instruções para a IA; os trechos com % são trocados pela
      // média mínima, pela frequência mínima e pela situação em JSON
      return """
             Você é um professor orientador. Elabore um plano de recuperação \
             de quatro semanas para o aluno, considerando que a aprovação \
             exige média mínima de %.1f e frequência mínima de %.0f%%. Se o \
             aluno já atende aos dois critérios, proponha atividades de \
             aprofundamento.

             Situação atual:
             %s
             """.formatted(CalculadoraSituacao.MEDIA_MINIMA,
                           CalculadoraSituacao.FREQUENCIA_MINIMA,
                           JsonSupport.toJson(resultado));
   }
}
