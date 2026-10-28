// pacote (pasta) com o código que conversa com o banco SQLite
package br.edu.ifpr.mcp_academico.persistence.jdbc;

// importa as classes do Java para arquivos, pastas e erros de leitura
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

// prepara o banco de dados quando o servidor é iniciado
public final class DatabaseBootstrap {
   // construtor privado: esta classe só tem métodos estáticos e não deve
   // ser criada com "new"
   private DatabaseBootstrap() {}

   // abre o banco de dados, criando-o com os dados de exemplo se ainda não
   // existir
   public static Database open(Path databaseFile) {
      // cria a pasta do banco, se ela ainda não existir
      createDirectories(databaseFile);

      // cria o objeto que acessa o banco
      Database          database    = new Database(
                                         new ConnectionFactory(databaseFile));
      // cria o objeto que prepara as tabelas e os dados de exemplo
      SchemaInitializer initializer = new SchemaInitializer(database);

      // cria as tabelas que ainda não existem
      initializer.createSchema();
      // coloca os dados de exemplo se o banco estiver vazio
      initializer.seedIfEmpty();

      // devolve o banco pronto para uso
      return database;
   }

   // cria a pasta onde o arquivo do banco vai ficar
   private static void createDirectories(Path databaseFile) {
      // pega a pasta do arquivo e cria essa pasta (e as pastas acima dela)
      try { Files.createDirectories(databaseFile.toAbsolutePath().getParent()); }
      // se não conseguir criar a pasta, avisa o erro
      catch (IOException ex) {
         throw new UncheckedIOException("Não foi possível criar o diretório " +
                                        "do banco de dados",ex);
      }
   }
}
