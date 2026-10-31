// pacote (pasta) dos resources do servidor MCP
package br.edu.ifpr.mcp_academico.server.resources;

// importa o Locale, que define as regras de idioma (como vírgula decimal)
import java.util.Locale;

// importa a calculadora (com as notas mínimas) e as classes de apoio
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.server.support.McpResourceDefinition;
import br.edu.ifpr.mcp_academico.server.support.ProtocolErrors;
import br.edu.ifpr.mcp_academico.server.support.ResourceResults;
// importa as classes da biblioteca MCP usadas para criar resources
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.Resource;

// resource que explica as regras de aprovação
public class RegrasAprovacaoResource implements McpResourceDefinition {
   // endereço do resource
   public static final String URI = "academico://regras/aprovacao";

   // tipo do conteúdo devolvido: texto em Markdown
   private static final String MIME_TYPE = "text/markdown";
   // texto das regras. Os trechos %.1f e %.0f%% são trocados pela média e
   // pela frequência mínimas quando o texto é montado
   private static final String REGRAS    = """
         # Critérios de aprovação

         O aluno é aprovado na disciplina quando atende aos dois critérios:

         - média ponderada das avaliações igual ou superior a %.1f;
         - frequência igual ou superior a %.0f%% das aulas registradas.

         ## Situações

         - EM_CURSO: ainda não há notas ou frequência registradas;
         - APROVADO: atende aos dois critérios;
         - REPROVADO_NOTA: frequência suficiente e média insuficiente;
         - REPROVADO_FREQUENCIA: frequência insuficiente, qualquer que seja
           a média;
         - CANCELADA: a matrícula foi cancelada.

         Estes critérios são uma premissa deste projeto e não reproduzem o
         regulamento de uma instituição de ensino.
         """;

   // transforma este resource no formato que a biblioteca MCP registra
   @Override
   public SyncResourceSpecification specification() {
      // descreve o resource: endereço, nome, título, descrição e tipo
      Resource resource = Resource.builder(URI,"regras-aprovacao")
                                  .title("Regras de aprovação")
                                  .description("Critérios usados para " +
                                               "calcular a situação do aluno")
                                  .mimeType(MIME_TYPE)
                                  .build();

      // liga a descrição ao código que monta o conteúdo quando o cliente lê;
      // "guard" transforma erros em respostas de erro do protocolo
      return new SyncResourceSpecification(resource,(exchange,request) ->
                     ProtocolErrors.guard(() -> ResourceResults.text(
                                        request.uri(),MIME_TYPE,regras())));
   }

   // monta o texto das regras com os valores mínimos atuais, no formato
   // brasileiro (6,0 em vez de 6.0)
   private String regras() {
      return String.format(Locale.of("pt","BR"),REGRAS,
                           CalculadoraSituacao.MEDIA_MINIMA,
                           CalculadoraSituacao.FREQUENCIA_MINIMA);
   }
}
