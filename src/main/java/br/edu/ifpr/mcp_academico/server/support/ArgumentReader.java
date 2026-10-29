// pacote (pasta) com as classes de apoio do servidor
package br.edu.ifpr.mcp_academico.server.support;

// importa as classes do Java para listas, mapas e valores opcionais
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// importa o erro da aplicação e os códigos de erro
import br.edu.ifpr.mcp_academico.domain.exception.DomainException;
import br.edu.ifpr.mcp_academico.domain.exception.ErrorCode;

// recupera os argumentos enviados pelo cliente MCP convertendo-os para tipos
// Java
//
// OBSERVAÇÃO:
// o SDK valida o esquema JSON somente dos argumentos de ferramentas. Argumentos
// de prompts chegam sem validação e sempre como texto, assim como as variáveis
// de URIs de resources, por isso os números também são aceitos nesse formato
public final class ArgumentReader {
   // argumentos recebidos: o nome de cada um ligado ao seu valor
   private final Map<String, Object> arguments;

   // guarda os argumentos; se vierem vazios (null), usa um mapa vazio
   public ArgumentReader(Map<String, Object> arguments) {
      this.arguments = arguments != null ? arguments : Map.of();
   }

   // lê um texto obrigatório; avisa o erro se ele não foi informado
   public String requiredString(String name) {
      return optionalString(name).orElseThrow(() -> missing(name));
   }

   // lê um texto opcional; devolve vazio se ele não foi informado
   public Optional<String> optionalString(String name) {
      // pega o valor pelo nome
      Object value = arguments.get(name);

      // não informado: devolve vazio
      if (value == null)
         return Optional.empty();

      // informado, mas não é texto: avisa o erro
      if (!(value instanceof String text))
         throw invalid(name,"um texto");

      // texto em branco conta como vazio; senão, tira os espaços das pontas
      return text.isBlank() ? Optional.empty() : Optional.of(text.trim());
   }

   // lê um número inteiro obrigatório
   public int requiredInt(String name) {
      return optionalInt(name).orElseThrow(() -> missing(name));
   }

   // lê um número inteiro opcional
   public Optional<Integer> optionalInt(String name) {
      // pega o valor pelo nome
      Object value = arguments.get(name);

      // não informado: vazio; informado: converte para inteiro
      return value == null ? Optional.empty()
                           : Optional.of(toInt(name,value));
   }

   // lê um número decimal obrigatório, como uma nota 7.5
   public double requiredDouble(String name) {
      // pega o valor pelo nome
      Object value = arguments.get(name);

      // não informado: avisa o erro
      if (value == null)
         throw missing(name);

      // converte para número decimal
      return toDouble(name,value);
   }

   // lê um valor opcional de sim ou não (true ou false)
   public Optional<Boolean> optionalBoolean(String name) {
      // pega o valor pelo nome
      Object value = arguments.get(name);

      // não informado: devolve vazio
      if (value == null)
         return Optional.empty();

      // informado, mas não é true/false: avisa o erro
      if (!(value instanceof Boolean flag))
         throw invalid(name,"verdadeiro ou falso");

      // devolve o valor
      return Optional.of(flag);
   }

   // lê uma lista obrigatória de números inteiros, como IDs de alunos
   public List<Integer> requiredIntList(String name) {
      // converte cada item da lista para inteiro
      return requiredList(name).stream()
                               .map(item -> toInt(name,item))
                               .toList();
   }

   // lê uma lista obrigatória de objetos, como as notas de cada aluno
   public List<ArgumentReader> requiredObjectList(String name) {
      // transforma cada objeto da lista em um novo ArgumentReader
      return requiredList(name).stream()
                               .map(item -> toReader(name,item))
                               .toList();
   }

   // lê uma lista obrigatória, sem converter os itens
   private List<?> requiredList(String name) {
      // pega o valor pelo nome
      Object value = arguments.get(name);

      // não informado: avisa o erro
      if (value == null)
         throw missing(name);

      // informado, mas não é lista: avisa o erro
      if (!(value instanceof List<?> list))
         throw invalid(name,"uma lista");

      // devolve a lista
      return list;
   }

   // converte um valor para número inteiro
   private int toInt(String name, Object value) {
      // primeiro converte para número decimal
      double number = toDouble(name,value);

      // recusa números com casas decimais (como 2.5) ou grandes demais
      if (number != Math.rint(number) ||
          number < Integer.MIN_VALUE || number > Integer.MAX_VALUE)
         throw invalid(name,"um número inteiro");

      // converte para inteiro
      return (int) number;
   }

   // converte um valor (texto ou número) para número decimal
   private double toDouble(String name, Object value) {
      // se for texto, converte o texto; senão, trata como número
      double number = value instanceof String text ? parse(name,text)
                                                   : toNumber(name,value);

      // recusa valores especiais como "infinito" e "NaN" (não é número)
      if (!Double.isFinite(number))
         throw invalid(name,"um número");

      // devolve o número
      return number;
   }

   // confere se o valor é um número e devolve como decimal
   private double toNumber(String name, Object value) {
      // não é número: avisa o erro
      if (!(value instanceof Number number))
         throw invalid(name,"um número");

      // devolve o número como decimal
      return number.doubleValue();
   }

   // converte um texto como "7.5" em número
   private double parse(String name, String text) {
      // tenta converter; se o texto não for um número, avisa o erro
      try { return Double.parseDouble(text.trim()); }
      catch (NumberFormatException ex) { throw invalid(name,"um número"); }
   }

   // transforma um item da lista (um objeto JSON) em ArgumentReader
   private ArgumentReader toReader(String name, Object item) {
      // o item precisa ser um objeto (mapa); senão, avisa o erro
      if (!(item instanceof Map<?, ?> map))
         throw invalid(name,"uma lista de objetos");

      // cria um mapa novo para copiar os dados
      Map<String, Object> copy = new LinkedHashMap<>();

      // copia cada campo, garantindo que o nome do campo seja texto
      map.forEach((key,value) -> copy.put(String.valueOf(key),value));

      // devolve um leitor para esse objeto
      return new ArgumentReader(copy);
   }

   // cria o erro de argumento obrigatório que não foi informado
   private DomainException missing(String name) {
      return new DomainException(ErrorCode.ARGUMENTO_INVALIDO,
                                 "O argumento '" + name + "' é obrigatório.");
   }

   // cria o erro de argumento com tipo errado
   private DomainException invalid(String name, String expected) {
      return new DomainException(ErrorCode.ARGUMENTO_INVALIDO,
                                 "O argumento '" + name + "' deve ser " +
                                 expected + ".");
   }
}
