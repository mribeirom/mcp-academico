package br.edu.ifpr.mcp_academico.client.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.client.ScriptedConsole;

class ItemSelectorTest {
   private static final List<String> ITEMS = List.of("alfa","beta");

   private static Optional<String> pick(ScriptedConsole scripted) {
      return ItemSelector.pick(scripted.console(),"Itens",ITEMS,
                               Function.identity(),item -> "sobre " + item);
   }

   @Test
   void deveSelecionarPeloNumero() {
      assertEquals(Optional.of("beta"),
                   ItemSelector.select(ITEMS,"2",Function.identity()));
   }

   @Test
   void deveSelecionarPeloNomeSemDiferenciarMaiusculas() {
      assertEquals(Optional.of("alfa"),
                   ItemSelector.select(ITEMS,"ALFA",Function.identity()));
   }

   @Test
   void deveRetornarVazioParaNumeroForaDaLista() {
      assertEquals(Optional.empty(),
                   ItemSelector.select(ITEMS,"0",Function.identity()));
      assertEquals(Optional.empty(),
                   ItemSelector.select(ITEMS,"3",Function.identity()));
   }

   @Test
   void deveRetornarVazioParaNomeDesconhecido() {
      assertEquals(Optional.empty(),
                   ItemSelector.select(ITEMS,"gama",Function.identity()));
   }

   @Test
   void deveExibirAListaERetornarOItemEscolhido() {
      ScriptedConsole scripted = new ScriptedConsole("1");

      assertEquals(Optional.of("alfa"),pick(scripted));
      assertTrue(scripted.output().contains("1 - alfa"));
      assertTrue(scripted.output().contains("sobre beta"));
   }

   @Test
   void deveVoltarComEntradaEmBranco() {
      ScriptedConsole scripted = new ScriptedConsole("");

      assertEquals(Optional.empty(),pick(scripted));
   }

   @Test
   void deveInformarItemNaoEncontrado() {
      ScriptedConsole scripted = new ScriptedConsole("gama");

      assertEquals(Optional.empty(),pick(scripted));
      assertTrue(scripted.output().contains("Item não encontrado: gama"));
   }

   @Test
   void deveInformarListaVazia() {
      ScriptedConsole scripted = new ScriptedConsole();

      assertEquals(Optional.empty(),
                   ItemSelector.pick(scripted.console(),"Itens",
                                     List.<String>of(),Function.identity(),
                                     Function.identity()));
      assertTrue(scripted.output().contains("Nenhum item"));
   }

   @Test
   void deveOmitirDescricaoNula() {
      ScriptedConsole scripted = new ScriptedConsole("");

      ItemSelector.pick(scripted.console(),"Itens",ITEMS,Function.identity(),
                        item -> null);

      assertTrue(scripted.output().contains("1 - alfa"));
   }
}
