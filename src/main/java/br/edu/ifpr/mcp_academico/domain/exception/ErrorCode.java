// pacote (pasta) onde ficam os erros da aplicação
package br.edu.ifpr.mcp_academico.domain.exception;

// lista de todos os códigos de erro que o servidor pode devolver ao cliente
public enum ErrorCode {
   // --- erros nos dados informados ---
   // algum valor informado está errado
   ARGUMENTO_INVALIDO,
   // o limite de resultados está fora da faixa permitida
   LIMITE_INVALIDO,
   // o semestre não está no formato AAAA/S
   SEMESTRE_INVALIDO,
   // a data não está no formato AAAA-MM-DD
   DATA_INVALIDA,
   // --- erros sobre alunos ---
   // o e-mail não tem um formato válido
   EMAIL_INVALIDO,
   // já existe um aluno com o mesmo RA ou e-mail
   ALUNO_DUPLICADO,
   // nenhum aluno tem o ID informado
   ALUNO_NAO_ENCONTRADO,
   // --- erros sobre disciplinas ---
   // a carga horária não é um número positivo
   CARGA_INVALIDA,
   // já existe uma disciplina com o mesmo código
   DISCIPLINA_DUPLICADA,
   // nenhuma disciplina tem o código informado
   DISCIPLINA_NAO_ENCONTRADA,
   // a disciplina existe, mas não aceita novas matrículas
   DISCIPLINA_INATIVA,
   // --- erros sobre matrículas ---
   // o aluno já está matriculado na disciplina nesse semestre
   MATRICULA_DUPLICADA,
   // a matrícula procurada não existe
   MATRICULA_NAO_ENCONTRADA,
   // a matrícula já tinha sido cancelada antes
   MATRICULA_JA_CANCELADA,
   // o aluno não está matriculado na turma
   ALUNO_NAO_MATRICULADO,
   // o aluno não tem matrículas no período pedido
   SEM_MATRICULAS,
   // a turma não tem nenhum aluno matriculado
   TURMA_VAZIA,
   // --- erros no lançamento de notas ---
   // o peso da avaliação não é um número positivo
   PESO_INVALIDO,
   // a nota está fora da faixa de 0 a 10
   NOTA_FORA_DA_FAIXA,
   // já existe uma avaliação com esse nome na turma
   AVALIACAO_DUPLICADA,
   // --- falhas inesperadas ---
   // algo deu errado sem que o erro estivesse previsto
   ERRO_INTERNO;

   // coloca o código na frente da mensagem, como "[ALUNO_DUPLICADO] ...",
   // para que a IA consiga identificar qual erro aconteceu
   public String format(String message) {
      // name() devolve o nome do código, como "ALUNO_DUPLICADO"
      return "[" + name() + "] " + message;
   }
}
