// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// histórico escolar: o aluno e o resultado em todas as disciplinas que ele
// já cursou, de todos os semestres
public record HistoricoAluno(Aluno aluno,
                             List<ResultadoDisciplina> disciplinas) {}
