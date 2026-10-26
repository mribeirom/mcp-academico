// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para conexões e erros de banco
import java.sql.Connection;
import java.sql.SQLException;
// importa Function, que representa um "pedaço de código" passado como valor
import java.util.function.Function;

// executa trabalhos no banco de dados. Cada operação usa sua própria conexão:
// abrir uma conexão com o SQLite é barato e evita manter o arquivo bloqueado
// entre chamadas das ferramentas
public final class Database {
   // classe que abre as conexões
   private final ConnectionFactory connections;

   // recebe a classe que abre as conexões
   public Database(ConnectionFactory connections) {
      // guarda essa classe para usar nos outros métodos
      this.connections = connections;
   }

   // executa um trabalho simples no banco e devolve o resultado dele
   public <T> T execute(Function<SqlSession, T> work) {
      // abre a conexão; o "try" garante que ela será fechada no final
      try (Connection connection = connections.open()) {
         // executa o trabalho e devolve o resultado
         return work.apply(new SqlSession(connection));
      }
      // se o banco der erro, troca pelo erro de persistência da aplicação
      catch (SQLException ex) {
         throw new PersistenceException("Erro acessando o banco de dados",ex);
      }
   }

   // executa um trabalho dentro de uma transação: ou tudo é gravado, ou nada
   public <T> T transaction(Function<SqlSession, T> work) {
      // abre a conexão; o "try" garante que ela será fechada no final
      try (Connection connection = connections.open()) {
         // desliga a gravação automática, para gravar tudo só no final
         connection.setAutoCommit(false);

         // executa o trabalho e confirma ou desfaz as alterações
         return commitOrRollback(connection,work);
      }
      // se o banco der erro, troca pelo erro de persistência da aplicação
      catch (SQLException ex) {
         throw new PersistenceException("Erro acessando o banco de dados",ex);
      }
   }

   // executa o trabalho; se der certo, confirma (commit); se der erro,
   // desfaz tudo (rollback)
   private <T> T commitOrRollback(Connection connection,
                                  Function<SqlSession, T> work)
                 throws SQLException {
      try {
         // executa o trabalho
         T result = work.apply(new SqlSession(connection));

         // confirma as alterações no banco
         connection.commit();

         // devolve o resultado do trabalho
         return result;
      }
      catch (RuntimeException ex) {
         // algo deu errado: desfaz todas as alterações
         connection.rollback();
         // repassa o erro para quem chamou
         throw ex;
      }
   }
}
