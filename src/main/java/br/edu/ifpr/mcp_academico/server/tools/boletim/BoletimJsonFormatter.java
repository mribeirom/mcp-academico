// pacote (pasta) dos formatos de apresentação do boletim
package br.edu.ifpr.mcp_academico.server.tools.boletim;

// importa o boletim e a classe que converte objetos em JSON
import br.edu.ifpr.mcp_academico.domain.model.Boletim;
import br.edu.ifpr.mcp_academico.server.support.JsonSupport;

// apresenta o boletim no formato JSON
public class BoletimJsonFormatter implements BoletimFormatter {
   // transforma o boletim em texto JSON
   @Override
   public String format(Boletim boletim) {
      return JsonSupport.toJson(boletim);
   }
}
