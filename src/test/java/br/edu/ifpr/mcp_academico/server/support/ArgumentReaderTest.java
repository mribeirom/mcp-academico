package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;

class ArgumentReaderTest {
   private static ArgumentReader reader(String name, Object value) {
      return new ArgumentReader(Map.of(name,value));
   }

   private static void assertInvalid(String expectedText, Executable action) {
      DomainException ex = assertThrows(DomainException.class,action);

      assertEquals(ErrorCode.ARGUMENTO_INVALIDO,ex.code());
      assertTrue(ex.getMessage().contains(expectedText),ex.getMessage());
   }

   @Test
   void deveTratarArgumentosNulosComoVazios() {
      ArgumentReader reader = new ArgumentReader(null);

      assertEquals(Optional.empty(),reader.optionalString("a"));
   }

   // =========================== textos ===================================

   @Test
   void deveLerTextoObrigatorioSemEspacosNasPontas() {
      assertEquals("João",reader("nome","  João ").requiredString("nome"));
   }

   @Test
   void deveFalharSeTextoObrigatorioNaoFoiInformado() {
      assertInvalid("obrigatório",
                    () -> new ArgumentReader(Map.of()).requiredString("nome"));
   }

   @Test
   void deveTratarTextoEmBrancoComoNaoInformado() {
      assertEquals(Optional.empty(),reader("nome","   ").optionalString("nome"));
      assertInvalid("obrigatório",
                    () -> reader("nome","   ").requiredString("nome"));
   }

   @Test
   void deveFalharSeTextoNaoForString() {
      assertInvalid("um texto",() -> reader("nome",10).optionalString("nome"));
   }

   // =========================== inteiros =================================

   @Test
   void deveLerInteiroDeDiferentesImplementacoesDeNumber() {
      assertEquals(7,reader("id",7).requiredInt("id"));
      assertEquals(7,reader("id",7L).requiredInt("id"));
      assertEquals(7,reader("id",7.0).requiredInt("id"));
   }

   @Test
   void deveLerInteiroVindoDeTexto() {
      assertEquals(7,reader("id"," 7 ").requiredInt("id"));
   }

   @Test
   void deveRetornarVazioSeInteiroOpcionalNaoFoiInformado() {
      assertEquals(Optional.empty(),
                   new ArgumentReader(Map.of()).optionalInt("id"));
   }

   @Test
   void deveLerInteiroOpcionalInformado() {
      assertEquals(Optional.of(7),reader("id",7).optionalInt("id"));
   }

   @Test
   void deveFalharSeInteiroObrigatorioNaoFoiInformado() {
      assertInvalid("obrigatório",
                    () -> new ArgumentReader(Map.of()).requiredInt("id"));
   }

   @Test
   void deveFalharSeInteiroPossuirCasasDecimais() {
      assertInvalid("inteiro",() -> reader("id",1.5).requiredInt("id"));
   }

   @Test
   void deveFalharSeInteiroEstiverForaDosLimites() {
      assertInvalid("inteiro",
                    () -> reader("id",Integer.MAX_VALUE + 1L).requiredInt("id"));
      assertInvalid("inteiro",
                    () -> reader("id",Integer.MIN_VALUE - 1L).requiredInt("id"));
   }

   @Test
   void deveFalharSeInteiroNaoForNumero() {
      assertInvalid("um número",() -> reader("id","abc").requiredInt("id"));
      assertInvalid("um número",() -> reader("id",true).requiredInt("id"));
   }

   // =========================== números ==================================

   @Test
   void deveLerNumeroDecimal() {
      assertEquals(7.5,reader("nota",7.5).requiredDouble("nota"));
      assertEquals(7.5,reader("nota","7.5").requiredDouble("nota"));
   }

   @Test
   void deveFalharSeNumeroObrigatorioNaoFoiInformado() {
      assertInvalid("obrigatório",
                    () -> new ArgumentReader(Map.of()).requiredDouble("nota"));
   }

   @Test
   void deveFalharSeNumeroNaoForFinito() {
      assertInvalid("um número",
                    () -> reader("nota","NaN").requiredDouble("nota"));
      assertInvalid("um número",
                    () -> reader("nota",Double.POSITIVE_INFINITY)
                                                     .requiredDouble("nota"));
   }

   // =========================== booleanos ================================

   @Test
   void deveLerBooleanoOpcional() {
      assertEquals(Optional.of(true),
                   reader("ativas",true).optionalBoolean("ativas"));
      assertEquals(Optional.empty(),
                   new ArgumentReader(Map.of()).optionalBoolean("ativas"));
   }

   @Test
   void deveFalharSeBooleanoForInvalido() {
      assertInvalid("verdadeiro ou falso",
                    () -> reader("ativas","sim").optionalBoolean("ativas"));
   }

   // =========================== listas ===================================

   @Test
   void deveLerListaDeInteiros() {
      assertEquals(List.of(1,2,3),
                   reader("ids",List.of(1,2L,"3")).requiredIntList("ids"));
   }

   @Test
   void deveAceitarListaVazia() {
      assertEquals(List.of(),reader("ids",List.of()).requiredIntList("ids"));
   }

   @Test
   void deveFalharSeListaObrigatoriaNaoFoiInformada() {
      assertInvalid("obrigatório",
                    () -> new ArgumentReader(Map.of()).requiredIntList("ids"));
   }

   @Test
   void deveFalharSeArgumentoNaoForLista() {
      assertInvalid("uma lista",() -> reader("ids",1).requiredIntList("ids"));
   }

   @Test
   void deveFalharSeItemDaListaForInvalido() {
      assertInvalid("um número",
                    () -> reader("ids",Arrays.asList(1,null))
                                                     .requiredIntList("ids"));
   }

   @Test
   void deveLerListaDeObjetos() {
      List<ArgumentReader> notas =
                reader("notas",List.of(Map.of("aluno_id",1,"valor",7.5)))
                                                .requiredObjectList("notas");

      assertEquals(1,notas.size());
      assertEquals(1,notas.get(0).requiredInt("aluno_id"));
      assertEquals(7.5,notas.get(0).requiredDouble("valor"));
   }

   @Test
   void deveFalharSeItemNaoForObjeto() {
      assertInvalid("lista de objetos",
                    () -> reader("notas",List.of(1))
                                                .requiredObjectList("notas"));
   }
}
