// pacote (pasta) com os contratos de acesso ao banco de dados
package br.edu.ifpr.mcp_academico.persistence;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a classe que representa um aluno
import br.edu.ifpr.mcp_academico.domain.model.Aluno;

// contrato (interface) que diz quais operações com alunos o banco precisa
// oferecer. A classe JdbcAlunoRepository faz essas operações de verdade
public interface AlunoRepository {
   // grava um aluno novo e devolve o aluno com o ID criado pelo banco
   public Aluno save(String ra, String nome, String email);
   // procura um aluno pelo ID; o resultado fica vazio se não achar
   public Optional<Aluno> findById(int id);
   // responde se já existe um aluno com esse RA
   public boolean existsByRa(String ra);
   // responde se já existe um aluno com esse e-mail
   public boolean existsByEmail(String email);
   // busca alunos cujo nome, RA ou e-mail contenha o termo
   public List<Aluno> search(String termo, int limite);
}
