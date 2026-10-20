// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// resumo das presenças de um aluno: quantas aulas teve e em quantas ele
// esteve presente
public record ResumoFrequencia(int aulas, int presencas) {
   // responde "sim" (true) quando já existe pelo menos uma aula registrada
   public boolean registrada() {
      // se o número de aulas for maior que zero, há registro
      return aulas > 0;
   }

   // calcula a frequência em porcentagem, como 87.5 para 87,5%
   public double percentual() {
      // presenças vezes 100, dividido pelo total de aulas
      return presencas * 100.0 / aulas;
   }
}
