// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// situações possíveis de um aluno em uma disciplina
public enum Situacao {
   // ainda faltam notas ou presenças para dar o resultado
   EM_CURSO,
   // média e frequência suficientes
   APROVADO,
   // média abaixo do mínimo
   REPROVADO_NOTA,
   // frequência abaixo do mínimo
   REPROVADO_FREQUENCIA,
   // a matrícula foi cancelada
   CANCELADA
}
