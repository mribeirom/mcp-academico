// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// nota que o aluno tirou em uma avaliação: o nome da avaliação (como
// "Prova 1"), o peso dela no cálculo da média e o valor da nota
public record NotaAvaliacao(String avaliacao, double peso, double valor) {}
