// pacote (pasta) com os contratos de acesso ao banco de dados
package br.edu.ifpr.mcp_academico.persistence;

// importa o tipo List (lista) e o Optional (valor que pode estar vazio)
import java.util.List;
import java.util.Optional;

// importa a matrícula e os seus estados possíveis
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;

// contrato (interface) com as operações de matrículas no banco
public interface MatriculaRepository {
   // grava uma matrícula nova, já ativa, e devolve a matrícula com o ID
   public Matricula save(int alunoId, int disciplinaId, String semestre);
   // procura uma matrícula pelo ID; o resultado fica vazio se não achar
   public Optional<Matricula> findById(int id);
   // procura a matrícula de um aluno em uma disciplina, em um semestre
   public Optional<Matricula> find(int alunoId, int disciplinaId,
                                   String semestre);
   // lista todas as matrículas de um aluno
   public List<Matricula> findByAluno(int alunoId);
   // lista as matrículas ativas de uma turma (disciplina + semestre)
   public List<Matricula> findAtivasDaTurma(int disciplinaId, String semestre);
   // muda o status de uma matrícula (ativa ou cancelada)
   public void updateStatus(int id, StatusMatricula status);
}
