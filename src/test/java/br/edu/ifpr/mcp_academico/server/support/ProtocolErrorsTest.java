package br.edu.ifpr.mcp_academico.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;

class ProtocolErrorsTest {
   @Test
   void deveRetornarOResultadoDaAcao() {
      assertEquals("ok",ProtocolErrors.guard(() -> "ok"));
   }

   @Test
   void deveTraduzirErroDeDominioParaParametrosInvalidos() {
      McpError error = assertThrows(McpError.class,() ->
               ProtocolErrors.guard(() -> {
                  throw new DomainException(ErrorCode.ARGUMENTO_INVALIDO,"x");
               }));

      assertEquals(ErrorCodes.INVALID_PARAMS,error.getJsonRpcError().code());
      assertEquals("[ARGUMENTO_INVALIDO] x",error.getMessage());
   }

   @Test
   void deveOcultarDetalhesDeErroInesperado() {
      McpError error = assertThrows(McpError.class,() ->
               ProtocolErrors.guard(() -> {
                  throw new IllegalStateException("senha do banco");
               }));

      assertEquals(ErrorCodes.INTERNAL_ERROR,error.getJsonRpcError().code());
      assertTrue(error.getMessage().contains("ERRO_INTERNO"));
      assertFalse(error.getMessage().contains("senha do banco"));
   }
}
