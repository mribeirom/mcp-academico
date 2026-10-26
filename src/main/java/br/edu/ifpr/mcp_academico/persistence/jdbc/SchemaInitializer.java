// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para ler arquivos e trabalhar com listas
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// prepara o banco de dados, podendo ser executado em todas as inicializações do
// servidor sem alterar um banco de dados já existente
public final class SchemaInitializer {
   // arquivo com os comandos que criam as tabelas
   private static final String SCHEMA = "/db/schema.sql";
   // arquivo com os dados de exemplo (alunos, disciplinas, notas...)
   private static final String SEED   = "/db/seed.sql";

   // objeto que executa os comandos no banco
   private final Database database;

   // recebe o objeto que acessa o banco
   public SchemaInitializer(Database database) {
      // guarda esse objeto para usar nos outros métodos
      this.database = database;
   }

   // cria as tabelas que ainda não existem
   public void createSchema() {
      // executa todos os comandos do arquivo de tabelas em uma transação
      database.transaction(session -> {
         // executa cada comando, um de cada vez
         statements(SCHEMA).forEach(session::update);
         // a transação não precisa devolver nada
         return null;
      });
   }

   // coloca os dados de exemplo, mas só quando o banco ainda não tem alunos
   public void seedIfEmpty() {
      database.transaction(session -> {
         // conta quantos alunos existem no banco
         int alunos = session.queryOne("SELECT COUNT(*) FROM aluno",
                                       row -> row.getInt(1)).orElse(0);
         // se não há nenhum, executa os comandos dos dados de exemplo
         if (alunos == 0)
            statements(SEED).forEach(session::update);

         // a transação não precisa devolver nada
         return null;
      });
   }

   // lê um arquivo SQL e separa os seus comandos
   private List<String> statements(String resource) {
      // lê o arquivo e remove as linhas de comentário, que começam com "--"
      String script = read(resource).lines()
                                    .filter(line -> !line.startsWith("--"))
                                    .collect(Collectors.joining("\n"));

      // divide o texto a cada ";" e descarta os pedaços vazios
      return Arrays.stream(script.split(";"))
                   .filter(statement -> !statement.isBlank())
                   .toList();
   }

   // lê o conteúdo de um arquivo que está dentro do jar
   private String read(String resource) {
      // abre o arquivo; o "try" garante que ele será fechado no final
      try (InputStream input = getClass().getResourceAsStream(resource)) {
         // lê todos os bytes e transforma em texto com acentos (UTF-8)
         return new String(input.readAllBytes(),StandardCharsets.UTF_8);
      }
      // se não conseguir ler, avisa o erro
      catch (IOException ex) {
         throw new UncheckedIOException("Erro lendo " + resource,ex);
      }
   }
}
