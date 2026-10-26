package br.edu.ifpr.mcp_academico.persistence.jdbc;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

// cada teste de persistência recebe um banco de dados novo, em arquivo temporário
abstract class DatabaseTestSupport {
   @TempDir
   Path directory;

   Database database;

   @BeforeEach
   void createDatabase() {
      database = new Database(new ConnectionFactory(
                                              directory.resolve("teste.db")));
      new SchemaInitializer(database).createSchema();
   }
}
