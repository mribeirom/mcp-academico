package br.edu.ifpr.mcp_academico.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;

class SemestreTest {
   @Test
   void deveInterpretarUmSemestreValido() {
      Semestre semestre = Semestre.parse("2026/2");

      assertEquals(2026,semestre.ano());
      assertEquals(2,semestre.periodo());
      assertEquals("2026/2",semestre.toString());
   }

   @ParameterizedTest
   @ValueSource(strings = {"2026","2026/3","2026/0","26/1","2026-1","abc",""})
   void deveRejeitarFormatosInvalidos(String texto) {
      DomainException ex = assertThrows(DomainException.class,
                                        () -> Semestre.parse(texto));

      assertEquals(ErrorCode.SEMESTRE_INVALIDO,ex.code());
   }
}
