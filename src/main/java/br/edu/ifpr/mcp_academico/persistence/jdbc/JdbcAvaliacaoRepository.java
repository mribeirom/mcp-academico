// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa as classes de notas e o contrato que esta classe cumpre
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.persistence.AvaliacaoRepository;

// lê e grava avaliações e notas nas tabelas "avaliacao" e "nota"
public class JdbcAvaliacaoRepository implements AvaliacaoRepository {
   // transforma uma linha da consulta em um objeto NotaAvaliacao
   private static final RowMapper<NotaAvaliacao> MAPPER =
                       row -> new NotaAvaliacao(row.getString("descricao"),
                                                row.getDouble("peso"),
                                                row.getDouble("valor"));

   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public JdbcAvaliacaoRepository(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // responde se a turma já tem uma avaliação com esse nome
   @Override
   public boolean exists(int disciplinaId, String semestre, String descricao) {
      // procura a avaliação; "isPresent" é true quando ela existe
      return database.execute(session -> session.queryOne(
                  "SELECT 1 FROM avaliacao " +
                  "WHERE disciplina_id = ? AND semestre = ? AND descricao = ?",
                  row -> true,disciplinaId,semestre,descricao)).isPresent();
   }

   // grava a avaliação e todas as notas dentro de uma transação
   @Override
   public void saveWithNotas(int disciplinaId, String semestre,
                             String descricao, double peso,
                             List<NotaMatricula> notas) {
      // tudo dentro deste bloco é gravado junto, ou nada é gravado
      database.transaction(session -> {
         // grava a avaliação e guarda o ID que o banco criou
         int avaliacaoId = session.insert(
                  "INSERT INTO avaliacao " +
                  "(disciplina_id, semestre, descricao, peso) " +
                  "VALUES (?, ?, ?, ?)",
                  disciplinaId,semestre,descricao,peso);

         // grava cada nota, ligada à avaliação e à matrícula do aluno
         for (NotaMatricula nota : notas)
            session.update("INSERT INTO nota " +
                           "(avaliacao_id, matricula_id, valor) " +
                           "VALUES (?, ?, ?)",
                           avaliacaoId,nota.matriculaId(),nota.valor());

         // a transação não precisa devolver nada
         return null;
      });
   }

   // busca todas as notas de uma matrícula, na ordem em que foram lançadas
   @Override
   public List<NotaAvaliacao> findNotas(int matriculaId) {
      // junta (JOIN) a nota com a sua avaliação para pegar o nome e o peso
      return database.execute(session -> session.query(
                  "SELECT a.descricao, a.peso, n.valor " +
                  "FROM nota n JOIN avaliacao a ON a.id = n.avaliacao_id " +
                  "WHERE n.matricula_id = ? ORDER BY a.id",
                  MAPPER,matriculaId));
   }
}
