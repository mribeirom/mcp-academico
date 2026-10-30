// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa o erro da aplicação, os códigos de erro e o boletim
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
// importa o serviço de relatórios e as classes de apoio das ferramentas
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;
import br.edu.ifpr.mcp_academico.server.support.AbstractTool;
import br.edu.ifpr.mcp_academico.server.support.ArgumentReader;
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;
import br.edu.ifpr.mcp_academico.server.support.ToolResults;
// importa os formatadores do boletim (texto e JSON)
import br.edu.ifpr.mcp_academico.server.tools.boletim.BoletimFormatter;
import br.edu.ifpr.mcp_academico.server.tools.boletim.BoletimJsonFormatter;
import br.edu.ifpr.mcp_academico.server.tools.boletim.BoletimTextoFormatter;
// importa o resultado e as indicações de ferramenta da biblioteca MCP
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

// ferramenta que gera o boletim de um aluno em um semestre
public class GerarBoletimTool extends AbstractTool {
   // nome do formato de texto
   private static final String TEXTO = "texto";
   // nome do formato JSON
   private static final String JSON  = "json";

   // liga cada nome de formato ao formatador que monta o boletim nesse
   // formato (padrão de projeto Strategy)
   private static final Map<String, BoletimFormatter> FORMATTERS =
                                     Map.of(TEXTO,new BoletimTextoFormatter(),
                                            JSON,new BoletimJsonFormatter());

   // serviço que monta o boletim de verdade
   private final RelatorioService relatorios;

   // recebe o serviço de relatórios
   public GerarBoletimTool(RelatorioService relatorios) {
      // guarda o serviço para usar ao executar
      this.relatorios = relatorios;
   }

   // nome técnico da ferramenta
   @Override
   protected String name() {
      return "gerar_boletim";
   }

   // título mostrado ao usuário
   @Override
   protected String title() {
      return "Gerar boletim";
   }

   // descrição lida pela IA
   @Override
   protected String description() {
      return "Gera o boletim de um aluno em um semestre, com notas, média, " +
             "frequência e situação em cada disciplina";
   }

   // parâmetros obrigatórios: aluno, semestre e formato (texto ou json)
   @Override
   protected Map<String, Object> inputSchema() {
      return JsonSchemaBuilder.object()
                              .required(Parametros.ALUNO_ID,
                                        Parametros.alunoId())
                              .required(Parametros.SEMESTRE,
                                        Parametros.semestre())
                              .required("formato",JsonSchemaBuilder.enumeration(
                                        "Formato do boletim",TEXTO,JSON))
                              .build();
   }

   // esta ferramenta só lê dados
   @Override
   protected ToolAnnotations annotations() {
      return READ_ONLY;
   }

   // gera o boletim
   @Override
   protected CallToolResult execute(ArgumentReader arguments) {
      // escolhe o formatador do formato pedido
      BoletimFormatter formatter = formatter(
                                       arguments.requiredString("formato"));
      // monta o boletim do aluno no semestre
      Boletim          boletim   = relatorios.boletim(
                        arguments.requiredInt(Parametros.ALUNO_ID),
                        arguments.requiredString(Parametros.SEMESTRE));

      // devolve o boletim formatado e também os dados estruturados
      return ToolResults.structured(formatter.format(boletim),
                                    JsonSupport.toMap(boletim));
   }

   // procura o formatador pelo nome do formato
   private BoletimFormatter formatter(String formato) {
      // pega o formatador no mapa
      BoletimFormatter formatter = FORMATTERS.get(formato);

      // formato desconhecido: avisa o erro
      if (formatter == null)
         throw new DomainException(ErrorCode.ARGUMENTO_INVALIDO,
                                   "O formato '" + formato + "' não existe.");

      // devolve o formatador encontrado
      return formatter;
   }
}
