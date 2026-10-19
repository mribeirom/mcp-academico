// pacote (pasta) onde ficam os erros da aplicação
package br.edu.ifpr.mcp_academico.domain.exception;

// erro previsto nas regras da aplicação, como "aluno não encontrado". A
// mensagem desse erro é segura para ser mostrada ao cliente MCP
public class DomainException extends RuntimeException {
   // número de versão exigido pelo Java para classes de erro
   private static final long serialVersionUID = 1L;

   // código que identifica o tipo do erro, como ALUNO_NAO_ENCONTRADO
   private final ErrorCode code;

   // cria o erro com o seu código e a mensagem explicando o que aconteceu
   public DomainException(ErrorCode code, String message) {
      // entrega a mensagem para a classe mãe (RuntimeException) guardar
      super(message);
      // guarda o código do erro
      this.code = code;
   }

   // devolve o código do erro
   public ErrorCode code() {
      return code;
   }
}
