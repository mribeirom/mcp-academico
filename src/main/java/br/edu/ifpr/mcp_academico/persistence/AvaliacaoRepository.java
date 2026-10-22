// pacote (pasta) com os contratos de acesso ao banco de dados
package br.edu.ifpr.mcp_academico.persistence;

// importa o tipo List, que guarda uma lista de itens
import java.util.List;

// importa as classes que representam as notas
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;

// contrato (interface) com as operações de avaliações e notas no banco
public interface AvaliacaoRepository {
   // responde se a turma já tem uma avaliação com esse nome
   public boolean exists(int disciplinaId, String semestre, String descricao);
   // grava a avaliação e suas notas em uma única transação: ou tudo é gravado
   // ou nada é gravado
   public void saveWithNotas(int disciplinaId, String semestre,
                             String descricao, double peso,
                             List<NotaMatricula> notas);
   // busca todas as notas de uma matrícula
   public List<NotaAvaliacao> findNotas(int matriculaId);
}
