package br.edu.ifpr.mcp_academico.server.tools.boletim;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoDisciplina;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;

class BoletimTextoFormatterTest {
   private final BoletimTextoFormatter formatter = new BoletimTextoFormatter();

   private String format(ResultadoDisciplina... disciplinas) {
      return formatter.format(new Boletim(
                   new Aluno(1,"2024001","Ana Conceição","ana@example.com"),
                   "2026/1",List.of(disciplinas)));
   }

   @Test
   void deveExibirOsDadosDoAlunoENotasNoPadraoBrasileiro() {
      String text = format(new ResultadoDisciplina(
                   "SWEB","Serviços Web","2026/1",
                   List.of(new NotaAvaliacao("Prova 1",4,8.5)),
                   new ResultadoAcademico(8.5,87.5,Situacao.APROVADO)));

      assertTrue(text.contains("Ana Conceição (RA 2024001) - 2026/1"),text);
      assertTrue(text.contains("SWEB - Serviços Web"),text);
      assertTrue(text.contains("Prova 1 (peso 4,0): 8,50"),text);
      assertTrue(text.contains("Média: 8,50"),text);
      assertTrue(text.contains("Frequência: 87,50%"),text);
      assertTrue(text.contains("Situação: APROVADO"),text);
   }

   @Test
   void deveExibirTracoSemLancamentos() {
      String text = format(new ResultadoDisciplina(
                   "BD","Banco de Dados","2026/1",List.of(),
                   new ResultadoAcademico(null,null,Situacao.EM_CURSO)));

      assertTrue(text.contains("Média: - | Frequência: - | " +
                               "Situação: EM_CURSO"),text);
   }
}
