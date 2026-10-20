// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// nota já ligada à matrícula do aluno, pronta para ser gravada no banco: o
// ID da matrícula e o valor da nota
public record NotaMatricula(int matriculaId, double valor) {}
