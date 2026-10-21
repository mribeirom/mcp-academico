// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// boletim de um aluno em um semestre: o aluno, o semestre (como "2026/1") e
// o resultado em cada disciplina que ele cursou nesse semestre
public record Boletim(Aluno aluno, String semestre,
                      List<ResultadoDisciplina> disciplinas) {}
