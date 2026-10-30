// pacote (pasta) dos formatos de apresentação do boletim
package br.edu.ifpr.mcp_academico.server.tools.boletim;

// importa a classe que representa o boletim
import br.edu.ifpr.mcp_academico.domain.model.Boletim;

// padrão de projeto Strategy: cada implementação é um formato de apresentação
// do boletim
public interface BoletimFormatter {
   // transforma o boletim em texto no formato da implementação
   public String format(Boletim boletim);
}
