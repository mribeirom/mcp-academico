// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa o aluno e o contrato que esta classe cumpre
import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;

// lê e grava alunos na tabela "aluno" do banco
public class JdbcAlunoRepository implements AlunoRepository {
   // transforma uma linha da tabela em um objeto Aluno
   private static final RowMapper<Aluno> MAPPER =
                                row -> new Aluno(row.getInt("id"),
                                                 row.getString("ra"),
                                                 row.getString("nome"),
                                                 row.getString("email"));

   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public JdbcAlunoRepository(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // grava um aluno novo
   @Override
   public Aluno save(String ra, String nome, String email) {
      // executa o INSERT; os "?" são trocados pelos valores, com segurança
      int id = database.execute(session -> session.insert(
                  "INSERT INTO aluno (ra, nome, email) VALUES (?, ?, ?)",
                  ra,nome,email));

      // devolve o aluno com o ID que o banco criou
      return new Aluno(id,ra,nome,email);
   }

   // procura um aluno pelo ID
   @Override
   public Optional<Aluno> findById(int id) {
      // busca uma linha; o resultado fica vazio se não achar
      return database.execute(session -> session.queryOne(
                  "SELECT * FROM aluno WHERE id = ?",MAPPER,id));
   }

   // responde se já existe um aluno com esse RA
   @Override
   public boolean existsByRa(String ra) {
      return exists("SELECT 1 FROM aluno WHERE ra = ?",ra);
   }

   // responde se já existe um aluno com esse e-mail
   @Override
   public boolean existsByEmail(String email) {
      return exists("SELECT 1 FROM aluno WHERE email = ?",email);
   }

   // busca alunos pelo nome, RA ou e-mail
   @Override
   public List<Aluno> search(String termo, int limite) {
      // os "%" no começo e no fim fazem o LIKE achar o termo em qualquer
      // parte do texto
      String padrao = "%" + termo + "%";

      // busca os alunos, ordena pelo nome e limita a quantidade
      return database.execute(session -> session.query(
                  "SELECT * FROM aluno " +
                  "WHERE nome LIKE ? OR ra LIKE ? OR email LIKE ? " +
                  "ORDER BY nome LIMIT ?",
                  MAPPER,padrao,padrao,padrao,limite));
   }

   // executa uma consulta e responde se ela encontrou alguma linha
   private boolean exists(String sql, String value) {
      // "isPresent" é true quando a consulta achou pelo menos uma linha
      return database.execute(session -> session.queryOne(sql,row -> true,value))
                     .isPresent();
   }
}
