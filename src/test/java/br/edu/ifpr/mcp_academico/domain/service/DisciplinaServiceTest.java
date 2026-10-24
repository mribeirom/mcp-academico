package br.edu.ifpr.mcp_academico.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;

class DisciplinaServiceTest {
   private final InMemoryRepositories.Disciplinas repositorio =
                                       new InMemoryRepositories.Disciplinas();
   private final DisciplinaService                service     =
                                       new DisciplinaService(repositorio);

   @Test
   void deveCadastrarDisciplinaComCodigoEmMaiusculas() {
      Disciplina disciplina = service.cadastrar("sweb","Serviços Web",80);

      assertEquals("SWEB",disciplina.codigo());
      assertEquals(80,disciplina.cargaHoraria());
      assertTrue(disciplina.ativa());
   }

   @ParameterizedTest
   @ValueSource(ints = {0,-10,1001})
   void deveRejeitarCargaHorariaInvalida(int carga) {
      DomainException ex = assertThrows(DomainException.class,
                              () -> service.cadastrar("SWEB","Serviços",carga));

      assertEquals(ErrorCode.CARGA_INVALIDA,ex.code());
   }

   @Test
   void deveRejeitarCodigoDuplicado() {
      service.cadastrar("SWEB","Serviços Web",80);

      DomainException ex = assertThrows(DomainException.class,
                              () -> service.cadastrar("sweb","Outra",40));

      assertEquals(ErrorCode.DISCIPLINA_DUPLICADA,ex.code());
   }

   @Test
   void deveListarTodasOuSomenteAsAtivas() {
      service.cadastrar("SWEB","Serviços Web",80);
      service.cadastrar("BD","Banco de Dados",80);
      repositorio.inativar(2);

      assertEquals(2,service.listar(false).size());
      assertEquals(1,service.listar(true).size());
   }
}
