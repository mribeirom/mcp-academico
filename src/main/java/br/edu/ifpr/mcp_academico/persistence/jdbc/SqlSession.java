// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para comandos SQL e listas
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// executa comandos SQL em uma conexão, evitando repetir nos repositórios o
// tratamento de PreparedStatement, ResultSet e SQLException
public final class SqlSession {
   // conexão aberta com o banco
   private final Connection connection;

   // recebe a conexão; só a classe Database cria objetos desta classe
   SqlSession(Connection connection) {
      // guarda a conexão para usar nos outros métodos
      this.connection = connection;
   }

   // executa uma consulta (SELECT) e devolve todas as linhas convertidas
   public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
      // prepara o comando e executa a consulta; o "try" fecha tudo no final
      try (PreparedStatement statement = prepare(sql,params);
           ResultSet         rows      = statement.executeQuery()) {
         // lista que vai receber os objetos
         List<T> result = new ArrayList<>();

         // passa por cada linha e converte em objeto
         while (rows.next())
            result.add(mapper.map(rows));

         // devolve a lista pronta
         return result;
      }
      // se o banco der erro, troca pelo erro da aplicação
      catch (SQLException ex) {
         throw failure(sql,ex);
      }
   }

   // executa uma consulta e devolve só a primeira linha (ou vazio)
   public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper,
                                   Object... params) {
      // usa o método anterior e pega o primeiro item da lista
      return query(sql,mapper,params).stream().findFirst();
   }

   // executa um comando que altera dados (INSERT, UPDATE, DELETE, CREATE) e
   // devolve quantas linhas foram alteradas
   public int update(String sql, Object... params) {
      // prepara o comando; o "try" fecha o comando no final
      try (PreparedStatement statement = prepare(sql,params)) {
         // executa e devolve a quantidade de linhas alteradas
         return statement.executeUpdate();
      }
      // se o banco der erro, troca pelo erro da aplicação
      catch (SQLException ex) {
         throw failure(sql,ex);
      }
   }

   // executa um INSERT e retorna o identificador gerado pelo banco de dados
   public int insert(String sql, Object... params) {
      // prepara o comando pedindo que o banco devolva o ID criado
      try (PreparedStatement statement =
               connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
         // coloca os valores no lugar dos "?"
         bind(statement,params);
         // executa o INSERT
         statement.executeUpdate();

         // lê o ID criado pelo banco
         try (ResultSet keys = statement.getGeneratedKeys()) {
            // vai para a primeira (e única) linha
            keys.next();
            // devolve o ID
            return keys.getInt(1);
         }
      }
      // se o banco der erro, troca pelo erro da aplicação
      catch (SQLException ex) {
         throw failure(sql,ex);
      }
   }

   // prepara um comando SQL e coloca os valores nos "?"
   private PreparedStatement prepare(String sql, Object... params)
                             throws SQLException {
      // cria o comando preparado a partir do texto SQL
      PreparedStatement statement = connection.prepareStatement(sql);

      // coloca os valores no lugar dos "?"
      bind(statement,params);

      // devolve o comando pronto para executar
      return statement;
   }

   // coloca cada valor no "?" correspondente. Usar "?" evita que um texto
   // digitado pelo usuário seja executado como comando (SQL injection)
   private void bind(PreparedStatement statement, Object... params)
                throws SQLException {
      // no JDBC, o primeiro "?" é o número 1, por isso o "i + 1"
      for (int i = 0; i < params.length; i++)
         statement.setObject(i + 1,params[i]);
   }

   // cria o erro da aplicação com o comando SQL que falhou
   private PersistenceException failure(String sql, SQLException ex) {
      return new PersistenceException("Erro executando SQL: " + sql,ex);
   }
}
