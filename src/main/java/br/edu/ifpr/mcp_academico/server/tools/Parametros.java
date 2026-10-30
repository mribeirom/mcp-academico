// pacote (pasta) das ferramentas do servidor MCP
package br.edu.ifpr.mcp_academico.server.tools;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// importa a classe que monta os esquemas dos parâmetros
import br.edu.ifpr.mcp_academico.server.support.JsonSchemaBuilder;

// parâmetros compartilhados por várias ferramentas, para não repetir o mesmo
// nome e a mesma descrição em vários lugares
final class Parametros {
   // nome do parâmetro com o ID do aluno
   static final String ALUNO_ID      = "aluno_id";
   // nome do parâmetro com o ID da disciplina
   static final String DISCIPLINA_ID = "disciplina_id";
   // nome do parâmetro com o semestre
   static final String SEMESTRE      = "semestre";
   // nome do parâmetro com a quantidade máxima de resultados
   static final String LIMITE        = "limite";

   // construtor privado: esta classe não deve ser criada com "new"
   private Parametros() {}

   // esquema do parâmetro com o ID do aluno (número inteiro)
   static Map<String, Object> alunoId() {
      return JsonSchemaBuilder.integer("ID do aluno");
   }

   // esquema do parâmetro com o ID da disciplina (número inteiro)
   static Map<String, Object> disciplinaId() {
      return JsonSchemaBuilder.integer("ID da disciplina");
   }

   // esquema do parâmetro com o semestre (texto)
   static Map<String, Object> semestre() {
      return JsonSchemaBuilder.string("Semestre no formato AAAA/S, como 2026/2");
   }
}
