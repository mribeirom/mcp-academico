// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// importa as classes do Java que conferem se um texto segue um padrão
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// importa o erro da aplicação e a lista de códigos de erro
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;

// período letivo no formato AAAA/S, como 2026/2: o ano e o período (1 ou 2)
public record Semestre(int ano, int periodo) {
   // padrão aceito: quatro dígitos, uma barra e o número 1 ou 2
   private static final Pattern FORMATO = Pattern.compile("(\\d{4})/([12])");

   // transforma um texto como "2026/2" em um objeto Semestre
   public static Semestre parse(String texto) {
      // prepara a comparação do texto com o padrão
      Matcher matcher = FORMATO.matcher(texto);

      // se o texto não segue o padrão, avisa o erro
      if (!matcher.matches())
         throw new DomainException(ErrorCode.SEMESTRE_INVALIDO,
                                   "O semestre '" + texto + "' deve estar no " +
                                   "formato AAAA/S, como 2026/2.");

      // pega o ano (primeira parte) e o período (segunda parte) do texto e
      // cria o semestre com esses dois números
      return new Semestre(Integer.parseInt(matcher.group(1)),
                          Integer.parseInt(matcher.group(2)));
   }

   // transforma o semestre de volta em texto, como "2026/2"
   @Override
   public String toString() {
      // junta o ano, uma barra e o período
      return ano + "/" + periodo;
   }
}
