package br.edu.ifpr.mcp_academico.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.client.McpAcademicoClient;
import br.edu.ifpr.mcp_academico.client.ScriptedConsole;
import br.edu.ifpr.mcp_academico.client.SdkMcpGateway;
import br.edu.ifpr.mcp_academico.persistence.jdbc.DatabaseLocation;

// executa o menu do cliente com o servidor real, digitando como um usuário
class ClienteIT {
   private static final File SERVER_JAR =
                                new File("server/mcp_academico_server.jar");

   private static SdkMcpGateway gateway;

   @BeforeAll
   static void conectar() throws Exception {
      Path directory = Files.createTempDirectory("mcp-academico-cliente-it");

      gateway = SdkMcpGateway.connect(SERVER_JAR,Map.of(
                       DatabaseLocation.VARIABLE,
                       directory.resolve("academico.db").toString()));
   }

   @AfterAll
   static void desconectar() {
      if (gateway != null)
         gateway.close();
   }

   private static String run(String... lines) {
      ScriptedConsole scripted = new ScriptedConsole(lines);

      McpAcademicoClient.menu(gateway,scripted.console()).run();

      return scripted.output();
   }

   @Test
   void deveListarAsFerramentasDoServidor() {
      String output = run("1","","sair");

      assertTrue(output.contains("cadastrar_aluno"),output);
      assertTrue(output.contains("estatisticas_disciplina"),output);
   }

   @Test
   void deveExecutarFerramentaComEnumeracao() {
      String output = run("1","gerar_boletim","1","2026/1","texto","sair");

      assertTrue(output.contains("Erro: Não"),output);
      assertTrue(output.contains("Ana Luíza Gonçalves"),output);
   }

   @Test
   void deveExecutarFerramentaComListaDeObjetos() {
      String output = run("1","lancar_notas","1","2026/2","Trabalho",
                          "2","sim","7","9,5","não","sair");

      assertTrue(output.contains("Erro: Não"),output);
      assertTrue(output.contains("notas_lancadas"),output);
   }

   @Test
   void deveExecutarFerramentaComListaDeInteiros() {
      String output = run("1","registrar_frequencia","1","2026/2",
                          "2026-08-10","8","sair");

      assertTrue(output.contains("Erro: Não"),output);
      assertTrue(output.contains("alunos_registrados"),output);
   }

   @Test
   void deveExibirErroDeNegocioEContinuar() {
      String output = run("1","calcular_situacao","999","1","2026/1","sair");

      assertTrue(output.contains("Erro: Sim"),output);
      assertTrue(output.contains("MATRICULA_NAO_ENCONTRADA"),output);
      assertFalse(output.contains("Exception"),output);
   }

   @Test
   void deveLerResourceDeRegras() {
      String output = run("2","academico://regras/aprovacao","sair");

      assertTrue(output.contains("Critérios de aprovação"),output);
   }

   @Test
   void deveLerResourceTemplateComAVariavelDigitada() {
      String output = run("2","academico://alunos/{id}/historico","2","sair");

      assertTrue(output.contains("João Pedro Conceição"),output);
   }

   @Test
   void deveObterPrompt() {
      String output = run("3","analisar_desempenho","7","2026/2","sair");

      assertTrue(output.contains("[USER]"),output);
   }

   @Test
   void deveExibirFalhaDoProtocoloSemEncerrar() {
      String output = run("3","analisar_desempenho","abc","2026/2","sair");

      assertTrue(output.contains("Erro:"),output);
      assertTrue(output.contains("ARGUMENTO_INVALIDO"),output);
   }
}
