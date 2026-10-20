// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// desempenho do aluno em uma disciplina, usado no boletim e no histórico: o
// código e o nome da disciplina, o semestre, as notas de cada avaliação e o
// resultado final (média, frequência e situação)
public record ResultadoDisciplina(String codigo, String disciplina,
                                  String semestre, List<NotaAvaliacao> notas,
                                  ResultadoAcademico resultado) {}
