// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa o tipo List (lista) e o Locale (regras de idioma)
import java.util.List;
import java.util.Locale;

// importa o erro da aplicação, os códigos de erro, a disciplina e o
// repositório (classe que lê e grava disciplinas no banco de dados)
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;

// regras para cadastrar e listar disciplinas
public class DisciplinaService {
   // maior carga horária aceita para uma disciplina
   private static final int CARGA_MAXIMA = 1000;

   // repositório usado para ler e gravar disciplinas no banco
   private final DisciplinaRepository disciplinas;

   // recebe o repositório que este serviço vai usar
   public DisciplinaService(DisciplinaRepository disciplinas) {
      // guarda o repositório para usar nos outros métodos
      this.disciplinas = disciplinas;
   }

   // cadastra uma disciplina nova e devolve a disciplina gravada
   public Disciplina cadastrar(String codigo, String nome, int cargaHoraria) {
      // passa o código para letras maiúsculas, como "sweb" para "SWEB"
      String codigoNormalizado = codigo.toUpperCase(Locale.ROOT);

      // a carga horária precisa estar entre 1 e o máximo permitido
      if (cargaHoraria < 1 || cargaHoraria > CARGA_MAXIMA)
         throw new DomainException(ErrorCode.CARGA_INVALIDA,
                                   "A carga horária deve estar entre 1 e " +
                                   CARGA_MAXIMA + " horas.");

      // se já existe uma disciplina com esse código, avisa o erro
      if (disciplinas.existsByCodigo(codigoNormalizado))
         throw new DomainException(ErrorCode.DISCIPLINA_DUPLICADA,
                                   "Já existe uma disciplina com o código '" +
                                   codigoNormalizado + "'.");

      // grava a disciplina no banco e devolve o resultado
      return disciplinas.save(codigoNormalizado,nome,cargaHoraria);
   }

   // lista as disciplinas; se "somenteAtivas" for true, ignora as inativas
   public List<Disciplina> listar(boolean somenteAtivas) {
      // pede a lista ao repositório
      return disciplinas.findAll(somenteAtivas);
   }
}
