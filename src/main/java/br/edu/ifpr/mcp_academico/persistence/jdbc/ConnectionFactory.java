// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para caminhos de arquivo e conexões com banco
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// abre conexões com o arquivo do banco de dados SQLite
public final class ConnectionFactory {
   // endereço do banco no formato do JDBC, como "jdbc:sqlite:C:/.../a.db"
   private final String     url;
   // configurações usadas em todas as conexões
   private final Properties properties = new Properties();

   // prepara o endereço e as configurações a partir do arquivo do banco
   public ConnectionFactory(Path databaseFile) {
      // monta o endereço com o caminho completo do arquivo
      this.url = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
      // o SQLite, por padrão, ignora chaves estrangeiras (as ligações entre
      // tabelas); esta linha liga essa conferência
      properties.setProperty("foreign_keys","true");
      // WAL e "busy_timeout" permitem que dois processos usem o mesmo arquivo,
      // como ocorre com o Claude Desktop aberto enquanto o cliente é executado.
      // O "busy_timeout" espera até 5 segundos se o arquivo estiver ocupado
      properties.setProperty("journal_mode","WAL");
      properties.setProperty("busy_timeout","5000");
      // transações obtêm o bloqueio de escrita logo no início
      properties.setProperty("transaction_mode","IMMEDIATE");
   }

   // abre e devolve uma nova conexão com o banco
   public Connection open() throws SQLException {
      // o DriverManager encontra o driver do SQLite e abre a conexão
      return DriverManager.getConnection(url,properties);
   }
}
