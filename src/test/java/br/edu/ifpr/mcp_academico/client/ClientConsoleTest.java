package br.edu.ifpr.mcp_academico.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ClientConsoleTest {
   @Test
   void deveExibirOPromptELerALinhaSemEspacosNasPontas() {
      ScriptedConsole scripted = new ScriptedConsole("  João  ");

      assertEquals(Optional.of("João"),scripted.console().readLine("Nome: "));
      assertTrue(scripted.output().contains("Nome: "));
   }

   @Test
   void deveRetornarVazioAoTerminarAEntrada() {
      ClientConsole console = new ScriptedConsole("única").console();

      console.readLine("> ");

      assertEquals(Optional.empty(),console.readLine("> "));
   }
}
