// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a matrícula, os seus estados e o contrato que esta classe cumpre
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;

// lê e grava matrículas na tabela "matricula" do banco
public class JdbcMatriculaRepository implements MatriculaRepository {
   // transforma uma linha da tabela em um objeto Matricula; o status é
   // gravado como texto ("ATIVA") e convertido de volta com "valueOf"
   private static final RowMapper<Matricula> MAPPER =
                row -> new Matricula(row.getInt("id"),
                                     row.getInt("aluno_id"),
                                     row.getInt("disciplina_id"),
                                     row.getString("semestre"),
                                     StatusMatricula.valueOf(
                                                     row.getString("status")));

   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public JdbcMatriculaRepository(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // grava uma matrícula nova, sempre ativa
   @Override
   public Matricula save(int alunoId, int disciplinaId, String semestre) {
      // executa o INSERT e guarda o ID que o banco criou
      int id = database.execute(session -> session.insert(
                  "INSERT INTO matricula " +
                  "(aluno_id, disciplina_id, semestre, status) " +
                  "VALUES (?, ?, ?, ?)",
                  alunoId,disciplinaId,semestre,StatusMatricula.ATIVA.name()));

      // devolve a matrícula com o ID criado
      return new Matricula(id,alunoId,disciplinaId,semestre,
                           StatusMatricula.ATIVA);
   }

   // procura uma matrícula pelo ID
   @Override
   public Optional<Matricula> findById(int id) {
      // busca uma linha; o resultado fica vazio se não achar
      return database.execute(session -> session.queryOne(
                  "SELECT * FROM matricula WHERE id = ?",MAPPER,id));
   }

   // procura a matrícula de um aluno em uma disciplina, em um semestre
   @Override
   public Optional<Matricula> find(int alunoId, int disciplinaId,
                                   String semestre) {
      // busca uma linha; o resultado fica vazio se não achar
      return database.execute(session -> session.queryOne(
                  "SELECT * FROM matricula " +
                  "WHERE aluno_id = ? AND disciplina_id = ? AND semestre = ?",
                  MAPPER,alunoId,disciplinaId,semestre));
   }

   // lista todas as matrículas de um aluno, por semestre e disciplina
   @Override
   public List<Matricula> findByAluno(int alunoId) {
      return database.execute(session -> session.query(
                  "SELECT * FROM matricula WHERE aluno_id = ? " +
                  "ORDER BY semestre, disciplina_id",
                  MAPPER,alunoId));
   }

   // lista as matrículas ativas de uma turma, em ordem de aluno
   @Override
   public List<Matricula> findAtivasDaTurma(int disciplinaId, String semestre) {
      return database.execute(session -> session.query(
                  "SELECT * FROM matricula " +
                  "WHERE disciplina_id = ? AND semestre = ? AND status = ? " +
                  "ORDER BY aluno_id",
                  MAPPER,disciplinaId,semestre,StatusMatricula.ATIVA.name()));
   }

   // muda o status de uma matrícula
   @Override
   public void updateStatus(int id, StatusMatricula status) {
      // grava o nome do status, como "CANCELADA", na matrícula do ID
      database.execute(session -> session.update(
                  "UPDATE matricula SET status = ? WHERE id = ?",
                  status.name(),id));
   }
}
