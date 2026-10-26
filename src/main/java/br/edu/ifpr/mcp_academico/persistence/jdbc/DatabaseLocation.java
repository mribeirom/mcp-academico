// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para caminhos de arquivo e mapas
import java.nio.file.Path;
import java.util.Map;

// decide onde fica o arquivo do banco. O servidor é executado com diretórios
// de trabalho diferentes pelo cliente, pelo Claude Desktop e pelo MCP
// Inspector, por isso o arquivo do banco de dados nunca é localizado com um
// caminho relativo
public final class DatabaseLocation {
   // nome da variável de ambiente que permite escolher outro arquivo
   public static final String VARIABLE = "ACADEMICO_DB_PATH";

   // construtor privado: esta classe não deve ser criada com "new"
   private DatabaseLocation() {}

   // devolve o caminho do banco a partir das variáveis de ambiente e da
   // pasta do usuário
   public static Path resolve(Map<String, String> environment,
                              String userHome) {
      // lê o caminho escolhido na variável de ambiente, se houver
      String configured = environment.get(VARIABLE);

      // sem variável, usa o padrão:
      // pasta do usuário/.mcp-academico/academico.db
      if (configured == null || configured.isBlank())
         return Path.of(userHome,".mcp-academico","academico.db");

      // com variável, usa o caminho informado, convertido em caminho completo
      return Path.of(configured).toAbsolutePath();
   }
}
