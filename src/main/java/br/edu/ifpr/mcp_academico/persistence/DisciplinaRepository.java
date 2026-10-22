// pacote (pasta) com os contratos de acesso ao banco de dados
package br.edu.ifpr.mcp_academico.persistence;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a classe que representa uma disciplina
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;

// contrato (interface) com as operações de disciplinas no banco
public interface DisciplinaRepository {
   // grava uma disciplina nova e devolve a disciplina com o ID criado
   public Disciplina save(String codigo, String nome, int cargaHoraria);
   // procura uma disciplina pelo ID; o resultado fica vazio se não achar
   public Optional<Disciplina> findById(int id);
   // responde se já existe uma disciplina com esse código
   public boolean existsByCodigo(String codigo);
   // lista as disciplinas, todas ou só as ativas
   public List<Disciplina> findAll(boolean somenteAtivas);
}
