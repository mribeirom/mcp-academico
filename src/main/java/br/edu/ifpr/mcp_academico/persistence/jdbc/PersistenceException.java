// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// falha de acesso ao banco de dados, não prevista nas regras da aplicação
public class PersistenceException extends RuntimeException {
   // número de versão exigido pelo Java para classes de erro
   private static final long serialVersionUID = 1L;

   // cria o erro com uma mensagem e o erro original do banco (a causa)
   public PersistenceException(String message, Throwable cause) {
      // entrega a mensagem e a causa para a classe mãe guardar
      super(message,cause);
   }
}
