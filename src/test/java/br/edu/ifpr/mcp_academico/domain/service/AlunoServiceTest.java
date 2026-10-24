package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Aluno;

class AlunoServiceTest {
   private final AlunoService service =
                         new AlunoService(new InMemoryRepositories.Alunos());

   private static void assertCode(ErrorCode code, Executable action) {
      assertEquals(code,assertThrows(DomainException.class,action).code());
   }

   @Test
   void deveCadastrarAlunoComEmailEmMinusculas() {
      Aluno aluno = service.cadastrar("Ana","ANA@Example.com","1");

      assertEquals("ana@example.com",aluno.email());
      assertEquals("Ana",aluno.nome());
      assertEquals("1",aluno.ra());
   }

   @ParameterizedTest
   @ValueSource(strings = {"ana","ana@","@example.com","ana@example",
                           "ana silva@example.com"})
   void deveRejeitarEmailInvalido(String email) {
      assertCode(ErrorCode.EMAIL_INVALIDO,
                 () -> service.cadastrar("Ana",email,"1"));
   }

   @Test
   void deveRejeitarRaDuplicado() {
      service.cadastrar("Ana","ana@example.com","1");

      assertCode(ErrorCode.ALUNO_DUPLICADO,
                 () -> service.cadastrar("Bia","bia@example.com","1"));
   }

   @Test
   void deveRejeitarEmailDuplicado() {
      service.cadastrar("Ana","ana@example.com","1");

      assertCode(ErrorCode.ALUNO_DUPLICADO,
                 () -> service.cadastrar("Bia","ANA@example.com","2"));
   }

   @Test
   void deveBuscarAlunosRespeitandoOLimite() {
      service.cadastrar("Ana","ana@example.com","1");
      service.cadastrar("Ana Clara","clara@example.com","2");
      service.cadastrar("Bia","bia@example.com","3");

      List<Aluno> encontrados = service.buscar("Ana",1);

      assertEquals(1,encontrados.size());
      assertEquals("Ana",encontrados.get(0).nome());
   }

   @Test
   void deveRetornarListaVaziaSemResultados() {
      assertEquals(List.of(),service.buscar("Zé",AlunoService.LIMITE_PADRAO));
   }

   @ParameterizedTest
   @ValueSource(ints = {0,-1,101})
   void deveRejeitarLimiteInvalido(int limite) {
      assertCode(ErrorCode.LIMITE_INVALIDO,() -> service.buscar("",limite));
   }
}
