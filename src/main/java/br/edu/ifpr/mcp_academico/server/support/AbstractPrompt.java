// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa o tipo List (lista) e o Map (mapa que liga uma chave a um valor)
import java.util.List;
import java.util.Map;

// importa as classes da biblioteca MCP usadas para criar prompts
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

// padrão de projeto Template Method: define os passos comuns a todos os
// prompts, deixando para as subclasses sua descrição e o texto a ser gerado
public abstract class AbstractPrompt implements McpPromptDefinition {
   // cada prompt informa o seu nome técnico, como "analisar_desempenho"
   protected abstract String name();
   // cada prompt informa o seu título, mostrado ao usuário
   protected abstract String title();
   // cada prompt informa a sua descrição
   protected abstract String description();
   // cada prompt informa os argumentos que pede, como o ID do aluno
   protected abstract List<PromptArgument> arguments();
   // cada prompt monta o seu texto a partir dos argumentos recebidos
   protected abstract String render(ArgumentReader arguments);

   // atalho para criar um argumento obrigatório com nome e descrição
   protected static PromptArgument required(String name, String description) {
      return PromptArgument.builder(name)
                           .description(description)
                           .required(true)
                           .build();
   }

   // transforma este prompt no formato que a biblioteca MCP registra
   @Override
   public final SyncPromptSpecification specification() {
      // descreve o prompt: nome, título, descrição e argumentos
      Prompt prompt = Prompt.builder(name())
                            .title(title())
                            .description(description())
                            .arguments(arguments())
                            .build();

      // liga a descrição ao código que gera o texto quando o cliente pede
      return new SyncPromptSpecification(prompt,(exchange,request) ->
                                                  get(request.arguments()));
   }

   // gera o texto do prompt. O SDK não valida os argumentos de um prompt:
   // isto é feito pelo ArgumentReader ao gerar o texto
   public final GetPromptResult get(Map<String, Object> arguments) {
      // "guard" transforma erros em respostas de erro do protocolo MCP
      return ProtocolErrors.guard(() -> {
         // pede à subclasse o texto, entregando os argumentos já organizados
         String        text    = render(new ArgumentReader(arguments));
         // coloca o texto em uma mensagem, como se o usuário tivesse escrito
         PromptMessage message = PromptMessage.builder(
                                       Role.USER,
                                       TextContent.builder(text).build())
                                              .build();

         // monta a resposta com a mensagem e a descrição do prompt
         return GetPromptResult.builder(List.of(message))
                               .description(description())
                               .build();
      });
   }
}
