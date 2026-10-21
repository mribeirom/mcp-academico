// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// números que resumem o desempenho de uma turma. Os campos do tipo Double
// (com D maiúsculo) ficam vazios (null) quando ainda não há notas
public record EstatisticasTurma(
      // total de alunos matriculados
      int alunos,
      // quantos alunos já têm nota
      int avaliados,
      // média das notas da turma
      Double media,
      // maior média entre os alunos
      Double maiorMedia,
      // menor média entre os alunos
      Double menorMedia,
      // quanto as médias se afastam da média da turma
      Double desvioPadrao,
      // quantos alunos foram aprovados
      int aprovados,
      // quantos foram reprovados por nota
      int reprovadosNota,
      // quantos foram reprovados por falta
      int reprovadosFrequencia,
      // quantos ainda não têm resultado final
      int emCurso,
      // percentual de aprovados entre os avaliados
      Double taxaAprovacao) {}
