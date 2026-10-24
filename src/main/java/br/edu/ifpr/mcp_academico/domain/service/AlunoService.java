// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa o tipo List (lista), o Locale (regras de idioma) e o Pattern
// (conferência de texto por padrão)
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// importa o erro da aplicação, os códigos de erro, o aluno e o repositório
// (classe que lê e grava alunos no banco de dados)
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;

// regras para cadastrar e buscar alunos
public class AlunoService {
   // quantidade de alunos devolvida na busca quando o usuário não informa
   public static final int LIMITE_PADRAO = 20;

   // maior quantidade de alunos que uma busca pode devolver
   private static final int     LIMITE_MAXIMO = 100;
   // padrão de e-mail: algo, arroba, algo, ponto, algo (como a@b.com)
   private static final Pattern EMAIL         =
                                  Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

   // repositório usado para ler e gravar alunos no banco
   private final AlunoRepository alunos;

   // recebe o repositório que este serviço vai usar
   public AlunoService(AlunoRepository alunos) {
      // guarda o repositório para usar nos outros métodos
      this.alunos = alunos;
   }

   // cadastra um aluno novo e devolve o aluno gravado, já com o seu ID
   public Aluno cadastrar(String nome, String email, String ra) {
      // passa o e-mail para letras minúsculas, para evitar duplicados como
      // "Ana@x.com" e "ana@x.com"
      String emailNormalizado = email.toLowerCase(Locale.ROOT);

      // se o e-mail não segue o padrão, avisa o erro
      if (!EMAIL.matcher(emailNormalizado).matches())
         throw new DomainException(ErrorCode.EMAIL_INVALIDO,
                                   "O e-mail '" + email + "' é inválido.");

      // se já existe um aluno com esse RA, avisa o erro
      if (alunos.existsByRa(ra))
         throw duplicado("RA",ra);

      // se já existe um aluno com esse e-mail, avisa o erro
      if (alunos.existsByEmail(emailNormalizado))
         throw duplicado("e-mail",emailNormalizado);

      // grava o aluno no banco e devolve o resultado
      return alunos.save(ra,nome,emailNormalizado);
   }

   // busca alunos pelo nome, RA ou e-mail, devolvendo no máximo "limite"
   public List<Aluno> buscar(String termo, int limite) {
      // o limite precisa estar entre 1 e o máximo permitido
      if (limite < 1 || limite > LIMITE_MAXIMO)
         throw new DomainException(ErrorCode.LIMITE_INVALIDO,
                                   "O limite deve estar entre 1 e " +
                                   LIMITE_MAXIMO + ".");

      // pede ao repositório para fazer a busca no banco
      return alunos.search(termo,limite);
   }

   // monta o erro de aluno duplicado, dizendo qual campo se repetiu
   private DomainException duplicado(String campo, String valor) {
      // cria o erro com o código ALUNO_DUPLICADO e uma mensagem explicativa
      return new DomainException(ErrorCode.ALUNO_DUPLICADO,
                                 "Já existe um aluno com o " + campo + " '" +
                                 valor + "'.");
   }
}
