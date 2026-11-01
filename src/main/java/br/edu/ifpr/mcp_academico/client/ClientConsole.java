// pacote (pasta) do cliente MCP
package br.edu.ifpr.mcp_academico.client;

// importa as classes do Java para entrada e saída de texto
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Scanner;

// entrada e saída do cliente, separadas de System.in e System.out para que as
// classes do cliente possam ser testadas com entradas programadas
public final class ClientConsole {
   // lê o que o usuário digita
   private final Scanner     input;
   // escreve na tela
   private final PrintStream output;

   // recebe a entrada, a codificação de caracteres (para os acentos) e a saída
   public ClientConsole(InputStream input, Charset charset, PrintStream output) {
      // cria o leitor de linhas com a codificação correta
      this.input  = new Scanner(input,charset);
      // guarda a saída
      this.output = output;
   }

   // mostra uma pergunta e lê a resposta do usuário. Retorna vazio quando não
   // há mais entradas, como ao final de um arquivo
   public Optional<String> readLine(String prompt) {
      // mostra a pergunta, sem pular linha
      output.print(prompt);
      // garante que a pergunta apareça na tela antes de esperar a resposta
      output.flush();

      // se há uma linha para ler, devolve ela sem espaços nas pontas
      return input.hasNextLine() ? Optional.of(input.nextLine().trim())
                                 : Optional.empty();
   }

   // escreve um texto e pula para a próxima linha
   public void println(String text) {
      output.println(text);
   }

   // escreve uma linha em branco
   public void println() {
      output.println();
   }
}
