// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a disciplina e o contrato que esta classe cumpre
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;

// lê e grava disciplinas na tabela "disciplina" do banco
public class JdbcDisciplinaRepository implements DisciplinaRepository {
   // transforma uma linha da tabela em um objeto Disciplina
   private static final RowMapper<Disciplina> MAPPER =
                          row -> new Disciplina(row.getInt("id"),
                                                row.getString("codigo"),
                                                row.getString("nome"),
                                                row.getInt("carga_horaria"),
                                                row.getBoolean("ativa"));

   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public JdbcDisciplinaRepository(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // grava uma disciplina nova
   @Override
   public Disciplina save(String codigo, String nome, int cargaHoraria) {
      // executa o INSERT e guarda o ID que o banco criou
      int id = database.execute(session -> session.insert(
                  "INSERT INTO disciplina (codigo, nome, carga_horaria) " +
                  "VALUES (?, ?, ?)",
                  codigo,nome,cargaHoraria));

      // devolve a disciplina nova, que sempre começa ativa
      return new Disciplina(id,codigo,nome,cargaHoraria,true);
   }

   // procura uma disciplina pelo ID
   @Override
   public Optional<Disciplina> findById(int id) {
      // busca uma linha; o resultado fica vazio se não achar
      return database.execute(session -> session.queryOne(
                  "SELECT * FROM disciplina WHERE id = ?",MAPPER,id));
   }

   // responde se já existe uma disciplina com esse código
   @Override
   public boolean existsByCodigo(String codigo) {
      // "isPresent" é true quando a consulta achou uma linha
      return database.execute(session -> session.queryOne(
                  "SELECT 1 FROM disciplina WHERE codigo = ?",
                  row -> true,codigo)).isPresent();
   }

   // lista as disciplinas em ordem de nome
   @Override
   public List<Disciplina> findAll(boolean somenteAtivas) {
      // monta o SQL; o filtro "ativa = 1" só entra quando pedido
      String sql = "SELECT * FROM disciplina " +
                   (somenteAtivas ? "WHERE ativa = 1 " : "") +
                   "ORDER BY nome";

      // executa a consulta e devolve a lista
      return database.execute(session -> session.query(sql,MAPPER));
   }
}
