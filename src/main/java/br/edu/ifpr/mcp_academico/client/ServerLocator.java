// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa as classes do Java para arquivos, caminhos e mapas
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

// localiza o servidor a partir do diretório do cliente, de modo que o cliente
// possa ser executado de qualquer diretório
public final class ServerLocator {
   // nome da variável de ambiente que permite escolher outro servidor
   public static final String VARIABLE = "MCP_SERVER_JAR";

   // caminho do servidor a partir da pasta do cliente (target/dist-client):
   // sobe duas pastas e entra em server
   private static final String RELATIVE_PATH =
                                        "../../server/mcp_academico_server.jar";

   // construtor privado: esta classe não deve ser criada com "new"
   private ServerLocator() {}

   // devolve o arquivo JAR do servidor
   public static File locate(Map<String, String> environment,
                             Path clientDirectory) {
      // lê o caminho escolhido na variável de ambiente, se houver
      String configured = environment.get(VARIABLE);
      // usa o caminho da variável ou, se não houver, o caminho padrão
      File   serverJar  =
                configured != null && !configured.isBlank()
                   ? new File(configured)
                   : clientDirectory.resolve(RELATIVE_PATH).normalize().toFile();
      // se o objeto File não é um arquivo: erro!!!
      if (!serverJar.isFile())
         throw new IllegalStateException("JAR do servidor não encontrado: " +
                                         serverJar.getAbsolutePath() +
                                         ". Gere o servidor ou informe o " +
                                         "caminho em " + VARIABLE + ".");
      // devolve o arquivo encontrado
      return serverJar;
   }
}
