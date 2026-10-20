// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// nota que o usuário informou para um aluno ao lançar uma avaliação: o ID do
// aluno e o valor da nota
public record NotaAluno(int alunoId, double valor) {}
