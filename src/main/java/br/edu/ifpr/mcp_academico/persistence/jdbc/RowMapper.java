// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa o resultado de consulta (ResultSet) e o erro de banco do Java
import java.sql.ResultSet;
import java.sql.SQLException;

// converte a linha atual do resultado de uma consulta em um objeto, como um
// Aluno. "@FunctionalInterface" permite escrever essa conversão em uma linha
// só, com "row -> ..."
@FunctionalInterface
public interface RowMapper<T> {
   // recebe a linha e devolve o objeto montado
   public T map(ResultSet row) throws SQLException;
}
