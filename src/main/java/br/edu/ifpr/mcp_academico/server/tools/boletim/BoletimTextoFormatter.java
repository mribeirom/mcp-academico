// pacote (pasta) dos formatos de apresentação do boletim
package br.edu.ifpr.mcp_academico.server.tools.boletim;

// importa o Locale, que define as regras de idioma (como vírgula decimal)
import java.util.Locale;

// importa as classes de dados usadas no boletim
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoDisciplina;

// apresenta o boletim como texto simples, fácil de ler
public class BoletimTextoFormatter implements BoletimFormatter {
   // regras do português do Brasil: números como 8,60 em vez de 8.60
   private static final Locale PT_BR         = Locale.of("pt","BR");
   // texto mostrado quando ainda não há média ou frequência
   private static final String SEM_LANCAMENTO = "-";

   // monta o texto do boletim inteiro
   @Override
   public String format(Boletim boletim) {
      // StringBuilder junta pedaços de texto de forma eficiente
      StringBuilder text = new StringBuilder();

      // primeira linha: nome, RA e semestre (%n é a quebra de linha)
      text.append(String.format("Boletim de %s (RA %s) - %s%n",
                                boletim.aluno().nome(),boletim.aluno().ra(),
                                boletim.semestre()));

      // acrescenta um bloco de texto para cada disciplina
      boletim.disciplinas().forEach(item -> append(text,item));

      // devolve o texto completo
      return text.toString();
   }

   // acrescenta o bloco de uma disciplina
   private void append(StringBuilder text, ResultadoDisciplina item) {
      // pega o resultado (média, frequência e situação) da disciplina
      ResultadoAcademico resultado = item.resultado();

      // linha em branco e depois o código e o nome da disciplina
      text.append(String.format("%n%s - %s%n",item.codigo(),item.disciplina()));
      // uma linha para cada nota
      item.notas().forEach(nota -> append(text,nota));
      // linha final com a média, a frequência e a situação
      text.append(String.format("  Média: %s | Frequência: %s | Situação: %s%n",
                                numero(resultado.media()),
                                percentual(resultado.frequencia()),
                                resultado.situacao()));
   }

   // acrescenta a linha de uma nota, como "  Prova 1 (peso 4,0): 8,00"
   private void append(StringBuilder text, NotaAvaliacao nota) {
      // %.1f mostra uma casa decimal e %.2f mostra duas
      text.append(String.format(PT_BR,"  %s (peso %.1f): %.2f%n",
                                nota.avaliacao(),nota.peso(),nota.valor()));
   }

   // formata um número com duas casas, ou "-" se ele estiver vazio
   private String numero(Double valor) {
      return valor == null ? SEM_LANCAMENTO
                           : String.format(PT_BR,"%.2f",valor);
   }

   // formata um percentual, como "87,50%", ou "-" se ele estiver vazio
   private String percentual(Double valor) {
      return valor == null ? SEM_LANCAMENTO : numero(valor) + "%";
   }
}
