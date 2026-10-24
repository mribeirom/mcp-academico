// pacote (pasta) onde ficam as regras de negócio da aplicação
package br.edu.ifpr.mcp_academico.domain.service;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa as classes de dados usadas no cálculo
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.ResultadoAcademico;
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;
import br.edu.ifpr.mcp_academico.domain.model.Situacao;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

// calcula a média, a frequência e a situação de um aluno em uma disciplina
public final class CalculadoraSituacao {
   // média mínima para ser aprovado
   public static final double MEDIA_MINIMA      = 6.0;
   // frequência mínima (em %) para ser aprovado
   public static final double FREQUENCIA_MINIMA = 75.0;

   // junta as notas e as presenças e devolve o resultado do aluno
   public ResultadoAcademico calcular(StatusMatricula status,
                                      List<NotaAvaliacao> notas,
                                      ResumoFrequencia frequencia) {
      // calcula a média das notas (fica vazia se não houver notas)
      Double media      = media(notas);
      // calcula a frequência em %, ou deixa vazia se não houver aulas
      Double percentual = frequencia.registrada()
                             ? arredondar(frequencia.percentual()) : null;

      // monta o resultado com a média, a frequência e a situação
      return new ResultadoAcademico(media,percentual,
                                    situacao(status,media,percentual));
   }

   // arredonda um número para duas casas decimais, como 8.567 para 8.57. Isso
   // também garante que a situação combine com a média mostrada ao usuário
   public static double arredondar(double valor) {
      // multiplica por 100, arredonda e divide por 100 de novo
      return Math.round(valor * 100.0) / 100.0;
   }

   // calcula a média ponderada (cada nota vale conforme o seu peso)
   private Double media(List<NotaAvaliacao> notas) {
      // sem notas, não há média
      if (notas.isEmpty())
         return null;

      // soma de cada nota multiplicada pelo seu peso
      double soma  = notas.stream().mapToDouble(n -> n.valor() * n.peso()).sum();
      // soma de todos os pesos
      double pesos = notas.stream().mapToDouble(NotaAvaliacao::peso).sum();

      // divide uma soma pela outra e arredonda o resultado
      return arredondar(soma / pesos);
   }

   // decide a situação do aluno a partir da matrícula, da média e da
   // frequência
   private Situacao situacao(StatusMatricula status, Double media,
                             Double frequencia) {
      // matrícula cancelada tem sempre a situação CANCELADA
      if (status == StatusMatricula.CANCELADA)
         return Situacao.CANCELADA;

      // se ainda falta a média ou a frequência, o aluno está EM_CURSO
      if (media == null || frequencia == null)
         return Situacao.EM_CURSO;

      // frequência abaixo do mínimo reprova por falta
      if (frequencia < FREQUENCIA_MINIMA)
         return Situacao.REPROVADO_FREQUENCIA;

      // média abaixo do mínimo reprova por nota; senão, o aluno é aprovado
      return media < MEDIA_MINIMA ? Situacao.REPROVADO_NOTA : Situacao.APROVADO;
   }
}
