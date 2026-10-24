package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

class CalculadoraSituacaoTest {
   private static final ResumoFrequencia PRESENTE =
                                                 new ResumoFrequencia(4,4);
   private static final ResumoFrequencia SEM_AULAS =
                                                 new ResumoFrequencia(0,0);

   private final CalculadoraSituacao calculadora = new CalculadoraSituacao();

   private static List<NotaAvaliacao> nota(double valor) {
      return List.of(new NotaAvaliacao("Prova",1,valor));
   }

   private ResultadoAcademico ativa(List<NotaAvaliacao> notas,
                                    ResumoFrequencia frequencia) {
      return calculadora.calcular(StatusMatricula.ATIVA,notas,frequencia);
   }

   @Test
   void deveCalcularMediaPonderadaPelosPesos() {
      List<NotaAvaliacao> notas = List.of(new NotaAvaliacao("P1",4,8.0),
                                          new NotaAvaliacao("P2",6,9.0));

      assertEquals(8.6,ativa(notas,PRESENTE).media());
   }

   @Test
   void deveArredondarParaDuasCasasDecimais() {
      List<NotaAvaliacao> notas = List.of(new NotaAvaliacao("P1",1,7.0),
                                          new NotaAvaliacao("P2",1,7.0),
                                          new NotaAvaliacao("P3",1,8.0));

      assertEquals(7.33,ativa(notas,PRESENTE).media());
   }

   @Test
   void deveCalcularOPercentualDeFrequencia() {
      assertEquals(66.67,ativa(nota(7),new ResumoFrequencia(3,2)).frequencia());
   }

   @Test
   void deveAprovarNoLimiteDaMediaEDaFrequencia() {
      ResultadoAcademico resultado = ativa(nota(6.0),new ResumoFrequencia(4,3));

      assertEquals(Situacao.APROVADO,resultado.situacao());
   }

   @Test
   void deveReprovarPorNotaAbaixoDaMedia() {
      assertEquals(Situacao.REPROVADO_NOTA,
                   ativa(nota(5.99),PRESENTE).situacao());
   }

   @Test
   void deveReprovarPorFrequenciaAbaixoDoMinimo() {
      assertEquals(Situacao.REPROVADO_FREQUENCIA,
                   ativa(nota(10),new ResumoFrequencia(100,74)).situacao());
   }

   @Test
   void deveMostrarReprovacaoPorFrequenciaMesmoComNotaBaixa() {
      assertEquals(Situacao.REPROVADO_FREQUENCIA,
                   ativa(nota(1),new ResumoFrequencia(2,0)).situacao());
   }

   @Test
   void deveFicarEmCursoSemNotas() {
      ResultadoAcademico resultado = ativa(List.of(),PRESENTE);

      assertEquals(Situacao.EM_CURSO,resultado.situacao());
      assertNull(resultado.media());
   }

   @Test
   void deveFicarEmCursoSemFrequencia() {
      ResultadoAcademico resultado = ativa(nota(9),SEM_AULAS);

      assertEquals(Situacao.EM_CURSO,resultado.situacao());
      assertNull(resultado.frequencia());
   }

   @Test
   void deveInformarMatriculaCanceladaIndependenteDasNotas() {
      ResultadoAcademico resultado =
                   calculadora.calcular(StatusMatricula.CANCELADA,nota(10),
                                        PRESENTE);

      assertEquals(Situacao.CANCELADA,resultado.situacao());
   }
}
