// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa a data do Java e o Map (mapa que liga uma chave a um valor)
import java.time.LocalDate;
import java.util.Map;

// importa o resumo de presenças e o contrato que esta classe cumpre
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;
import br.edu.ifpr.mcp_academico.persistence.FrequenciaRepository;

// lê e grava presenças na tabela "frequencia" do banco
public class JdbcFrequenciaRepository implements FrequenciaRepository {
   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public JdbcFrequenciaRepository(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // grava a presença ou falta de cada matrícula na data informada
   @Override
   public void save(LocalDate data, Map<Integer, Boolean> presencaPorMatricula) {
      // tudo é gravado junto, ou nada é gravado
      database.transaction(session -> {
         // para cada matrícula, grava 1 (presente) ou 0 (ausente). Se já
         // existir registro nessa data, o "ON CONFLICT" atualiza o valor
         presencaPorMatricula.forEach((matriculaId,presente) ->
            session.update("INSERT INTO frequencia " +
                           "(matricula_id, data, presente) VALUES (?, ?, ?) " +
                           "ON CONFLICT (matricula_id, data) " +
                           "DO UPDATE SET presente = excluded.presente",
                           matriculaId,data.toString(),presente ? 1 : 0));
         // a transação não precisa devolver nada
         return null;
      });
   }

   // conta as aulas e as presenças de uma matrícula
   @Override
   public ResumoFrequencia resumo(int matriculaId) {
      // COUNT conta as aulas; SUM soma os 1 das presenças (COALESCE troca
      // o vazio por 0 quando não há aulas)
      return database.execute(session -> session.queryOne(
                  "SELECT COUNT(*) AS aulas, " +
                  "COALESCE(SUM(presente), 0) AS presencas " +
                  "FROM frequencia WHERE matricula_id = ?",
                  row -> new ResumoFrequencia(row.getInt("aulas"),
                                              row.getInt("presencas")),
                  matriculaId)).orElseThrow();
   }
}
