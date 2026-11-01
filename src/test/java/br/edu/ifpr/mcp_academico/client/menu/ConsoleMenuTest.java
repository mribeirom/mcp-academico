package br.edu.ifpr.mcp_academico.client.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.client.ScriptedConsole;

class ConsoleMenuTest {
   // dublê de teste: opção do menu que conta suas execuções
   private static class FakeAction implements MenuAction {
      private final RuntimeException failure;
      private int                    executions;

      FakeAction(RuntimeException failure) {
         this.failure = failure;
      }

      @Override
      public String label() {
         return "Ação";
      }

      @Override
      public void execute() {
         executions++;

         if (failure != null)
            throw failure;
      }
   }

   private final FakeAction action = new FakeAction(null);

   private void run(ScriptedConsole scripted, MenuAction... actions) {
      new ConsoleMenu(scripted.console(),List.of(actions)).run();
   }

   @Test
   void deveExecutarAOpcaoPeloNumeroEPeloNome() {
      run(new ScriptedConsole("1","ação","sair"),action);

      assertEquals(2,action.executions);
   }

   @Test
   void deveEncerrarComOComandoSair() {
      ScriptedConsole scripted = new ScriptedConsole("SAIR","1");

      run(scripted,action);

      assertEquals(0,action.executions);
      assertTrue(scripted.output().contains("sair - encerrar o cliente"));
   }

   @Test
   void deveEncerrarAoTerminarAEntrada() {
      run(new ScriptedConsole("1"),action);

      assertEquals(1,action.executions);
   }

   @Test
   void deveIgnorarEntradaEmBranco() {
      run(new ScriptedConsole("","sair"),action);

      assertEquals(0,action.executions);
   }

   @Test
   void deveInformarOpcaoInexistente() {
      ScriptedConsole scripted = new ScriptedConsole("9","sair");

      run(scripted,action);

      assertTrue(scripted.output().contains("Opção não encontrada: 9"));
   }

   @Test
   void deveContinuarDepoisDeUmaFalha() {
      FakeAction      failing  = new FakeAction(
                                     new IllegalStateException("servidor"));
      ScriptedConsole scripted = new ScriptedConsole("1","1","sair");

      run(scripted,failing);

      assertEquals(2,failing.executions);
      assertTrue(scripted.output().contains("Erro: servidor"));
   }
}
