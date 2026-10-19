// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// matrícula de um aluno em uma disciplina, em um semestre. O status diz se
// ela está ativa ou cancelada
public record Matricula(int id, int alunoId, int disciplinaId, String semestre,
                        StatusMatricula status) {
   // responde "sim" (true) quando a matrícula não foi cancelada
   public boolean ativa() {
      // compara o status da matrícula com o valor ATIVA
      return status == StatusMatricula.ATIVA;
   }
}
