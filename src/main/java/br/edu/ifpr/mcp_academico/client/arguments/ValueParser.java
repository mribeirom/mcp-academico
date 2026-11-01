// pacote (pasta) com as classes que leem os argumentos digitados
package br.edu.ifpr.mcp_academico.client.arguments;

// importa o Map (mapa que liga uma chave a um valor)
import java.util.Map;

// padrão de projeto Strategy: converte o texto digitado pelo usuário no tipo
// definido pelo esquema JSON. Lança IllegalArgumentException se o valor for
// inválido
@FunctionalInterface
public interface ValueParser {
   // recebe o texto digitado e o esquema do parâmetro e devolve o valor
   public Object parse(String text, Map<?, ?> schema);
}
