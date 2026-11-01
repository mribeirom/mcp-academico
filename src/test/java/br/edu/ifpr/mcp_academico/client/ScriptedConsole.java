package br.edu.ifpr.mcp_academico.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

// dublê de teste: console com as linhas digitadas definidas pelo teste e cuja
// saída pode ser conferida
public final class ScriptedConsole {
   private final ByteArrayOutputStream output  = new ByteArrayOutputStream();
   private final ClientConsole         console;

   public ScriptedConsole(String... lines) {
      String input = lines.length == 0 ? "" : String.join("\n",lines) + "\n";

      console = new ClientConsole(
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8,
            new PrintStream(output,true,StandardCharsets.UTF_8));
   }

   public ClientConsole console() {
      return console;
   }

   public String output() {
      return output.toString(StandardCharsets.UTF_8);
   }
}
