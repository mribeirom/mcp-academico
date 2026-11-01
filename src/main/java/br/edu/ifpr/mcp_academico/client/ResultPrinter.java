// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa as classes da biblioteca MCP que representam as respostas
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

// exibe ao usuário as respostas do servidor MCP
public final class ResultPrinter {
   // linha usada para separar os blocos na tela
   private static final String LINE = "========================================";

   // objeto que escreve na tela
   private final ClientConsole console;

   // recebe o objeto que escreve na tela
   public ResultPrinter(ClientConsole console) {
      // guarda esse objeto para usar nos outros métodos
      this.console = console;
   }

   // mostra o resultado de uma ferramenta
   public void print(CallToolResult result) {
      // título do bloco
      header("RESULTADO");
      // informa se a ferramenta devolveu um erro
      console.println("Erro: " +
                      (Boolean.TRUE.equals(result.isError()) ? "Sim" : "Não"));
      // mostra cada conteúdo devolvido
      result.content().forEach(content -> console.println(textOf(content)));

      // se não veio texto, mostra os dados estruturados
      if (result.content().isEmpty() && result.structuredContent() != null)
         console.println(String.valueOf(result.structuredContent()));

      // linha de fechamento
      console.println(LINE);
   }

   // mostra o conteúdo de um resource
   public void print(ReadResourceResult result) {
      // título do bloco
      header("RESOURCE");
      // mostra cada conteúdo devolvido
      result.contents().forEach(content -> console.println(textOf(content)));
      // linha de fechamento
      console.println(LINE);
   }

   // mostra o texto gerado por um prompt
   public void print(GetPromptResult result) {
      // título do bloco
      header("PROMPT");
      // mostra cada mensagem, com o papel (como USER) entre colchetes
      result.messages().forEach(message ->
           console.println("[" + message.role() + "] " +
                           textOf(message.content())));
      // linha de fechamento
      console.println(LINE);
   }

   // mostra o título de um bloco, depois de uma linha em branco
   private void header(String title) {
      console.println();
      console.println("=============== " + title + " ===============");
   }

   // pega o texto de um conteúdo. Conteúdos não textuais, como imagens, são
   // exibidos pela sua descrição
   private String textOf(Content content) {
      return content instanceof TextContent text ? text.text()
                                                 : String.valueOf(content);
   }

   // pega o texto do conteúdo de um resource
   private String textOf(ResourceContents content) {
      return content instanceof TextResourceContents text
                ? text.text() : String.valueOf(content);
   }
}
