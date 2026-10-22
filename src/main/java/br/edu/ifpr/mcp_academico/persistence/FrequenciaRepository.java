// pacote (pasta) com os contratos de acesso ao banco de dados
package br.edu.ifpr.mcp_academico.persistence;

// importa a data do Java e o Map (mapa que liga uma chave a um valor)
import java.time.LocalDate;
import java.util.Map;

// importa a classe com o resumo das presenças
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;

// contrato (interface) com as operações de frequência no banco
public interface FrequenciaRepository {
   // grava a presença de cada matrícula na aula da data informada, substituindo
   // um registro anterior da mesma data
   public void save(LocalDate data, Map<Integer, Boolean> presencaPorMatricula);
   // conta as aulas e as presenças de uma matrícula
   public ResumoFrequencia resumo(int matriculaId);
}
