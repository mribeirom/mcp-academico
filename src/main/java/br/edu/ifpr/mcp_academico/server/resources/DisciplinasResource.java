// pacote (pasta) dos resources do servidor MCP
package br.edu.ifpr.mcp_academico.server.resources;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o serviço de disciplinas e as classes de apoio do servidor
import br.edu.ifpr.mcp_academico.domain.service.DisciplinaService;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
import br.edu.ifpr.mcp_academico.server.support.McpResourceDefinition;
import br.edu.ifpr.mcp_academico.server.support.ProtocolErrors;
import br.edu.ifpr.mcp_academico.server.support.ResourceResults;
// importa as classes da biblioteca MCP usadas para criar resources
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.Resource;

// resource com o catálogo de todas as disciplinas. Um resource é um dado
// que o cliente pode ler por um endereço (URI), como um arquivo
public class DisciplinasResource implements McpResourceDefinition {
   // endereço do resource
   public static final String URI = "academico://disciplinas";

   // tipo do conteúdo devolvido: JSON
   private static final String MIME_TYPE = "application/json";

   // serviço que lista as disciplinas
   private final DisciplinaService disciplinas;

   // recebe o serviço de disciplinas
   public DisciplinasResource(DisciplinaService disciplinas) {
      // guarda o serviço para usar na leitura
      this.disciplinas = disciplinas;
   }

   // transforma este resource no formato que a biblioteca MCP registra
   @Override
   public SyncResourceSpecification specification() {
      // descreve o resource: endereço, nome, título, descrição e tipo
      Resource resource = Resource.builder(URI,"disciplinas")
                                  .title("Catálogo de disciplinas")
                                  .description("Todas as disciplinas " +
                                               "cadastradas")
                                  .mimeType(MIME_TYPE)
                                  .build();

      // liga a descrição ao código que monta o conteúdo quando o cliente lê;
      // "guard" transforma erros em respostas de erro do protocolo
      return new SyncResourceSpecification(resource,(exchange,request) ->
                     ProtocolErrors.guard(() -> ResourceResults.text(
                                        request.uri(),MIME_TYPE,catalogo())));
   }

   // monta o JSON com todas as disciplinas, inclusive as inativas
   private String catalogo() {
      return JsonSupport.toJson(Map.of("disciplinas",
                                       disciplinas.listar(false)));
   }
}
