// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// resultado do aluno em uma disciplina: a média, a frequência (em %) e a
// situação. A média e a frequência ficam vazias (null) enquanto o professor
// não lançar notas ou presenças
public record ResultadoAcademico(Double media, Double frequencia,
                                 Situacao situacao) {}
