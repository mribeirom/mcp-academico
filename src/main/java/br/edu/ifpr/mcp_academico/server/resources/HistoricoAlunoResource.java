// pacote (pasta) dos resources do servidor MCP
package br.edu.ifpr.mcp_academico.server.resources;

// importa o HashMap, um mapa que liga uma chave a um valor
import java.util.HashMap;

// importa o serviço de relatórios e as classes de apoio do servidor
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
import br.edu.ifpr.mcp_academico.server.support.McpResourceTemplateDefinition;
import br.edu.ifpr.mcp_academico.server.support.ProtocolErrors;
import br.edu.ifpr.mcp_academico.server.support.ResourceResults;
// importa as classes da biblioteca MCP usadas para criar templates
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManagerFactory;
import io.modelcontextprotocol.util.McpUriTemplateManager;

// template de resource com o histórico de um aluno. O endereço tem uma parte
// variável, {id}, trocada pelo ID do aluno, como academico://alunos/1/historico
public class HistoricoAlunoResource implements McpResourceTemplateDefinition {
   // modelo do endereço, com a parte variável entre chaves
   public static final String URI_TEMPLATE =
                                           "academico://alunos/{id}/historico";

   // tipo do conteúdo devolvido: JSON
   private static final String MIME_TYPE = "application/json";

   // objeto que sabe extrair o {id} de um endereço real
   private final McpUriTemplateManager uriTemplate =
           new DefaultMcpUriTemplateManagerFactory().create(URI_TEMPLATE);
   // serviço que monta o histórico
   private final RelatorioService      relatorios;

   // recebe o serviço de relatórios
   public HistoricoAlunoResource(RelatorioService relatorios) {
      // guarda o serviço para usar na leitura
      this.relatorios = relatorios;
   }

   // transforma este template no formato que a biblioteca MCP registra
   @Override
   public SyncResourceTemplateSpecification specification() {
      // descreve o template: endereço, nome, título, descrição e tipo
      ResourceTemplate template =
                  ResourceTemplate.builder(URI_TEMPLATE,"historico-aluno")
                                  .title("Histórico do aluno")
                                  .description("Histórico acadêmico de um " +
                                               "aluno identificado pelo ID")
                                  .mimeType(MIME_TYPE)
                                  .build();

      // liga a descrição ao código que monta o conteúdo quando o cliente lê;
      // "guard" transforma erros em respostas de erro do protocolo
      return new SyncResourceTemplateSpecification(template,(exchange,request) ->
                               ProtocolErrors.guard(() -> read(request)));
   }

   // monta o conteúdo a partir do endereço pedido pelo cliente
   private ReadResourceResult read(ReadResourceRequest request) {
      // as variáveis presentes na URI são lidas como os demais argumentos
      ArgumentReader variables = new ArgumentReader(new HashMap<>(
                           uriTemplate.extractVariableValues(request.uri())));
      // lê o ID do aluno, que chega como texto, e converte para número
      int            id        = variables.requiredInt("id");

      // busca o histórico, converte para JSON e devolve
      return ResourceResults.text(request.uri(),MIME_TYPE,
                                  JsonSupport.toJson(relatorios.historico(id)));
   }
}
