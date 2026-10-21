// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// uma linha do ranking da turma: a posição do aluno, o ID e o nome dele, a
// média final e a situação (aprovado, reprovado etc.)
public record ClassificacaoTurma(int posicao, int alunoId, String nome,
                                 double media, Situacao situacao) {}
