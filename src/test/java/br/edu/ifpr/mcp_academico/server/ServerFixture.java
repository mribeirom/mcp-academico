package br.edu.ifpr.mcp_academico.server;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.service.CalculadoraSituacao;
import br.edu.ifpr.mcp_academico.domain.service.DisciplinaService;
import br.edu.ifpr.mcp_academico.domain.service.InMemoryRepositories;
import br.edu.ifpr.mcp_academico.domain.service.RelatorioService;

// dublê de teste: serviços sobre repositórios em memória, com uma turma pronta
public final class ServerFixture {
   public static final String SEMESTRE = "2026/1";

   public final DisciplinaService disciplinas;
   public final RelatorioService  relatorios;

   public ServerFixture() {
      InMemoryRepositories.Alunos      alunos      =
                                            new InMemoryRepositories.Alunos();
      InMemoryRepositories.Disciplinas catalogo    =
                                       new InMemoryRepositories.Disciplinas();
      InMemoryRepositories.Matriculas  matriculas  =
                                        new InMemoryRepositories.Matriculas();
      InMemoryRepositories.Avaliacoes  avaliacoes  =
                                        new InMemoryRepositories.Avaliacoes();
      InMemoryRepositories.Frequencias frequencias =
                                       new InMemoryRepositories.Frequencias();

      alunos.save("1","Ana Conceição","ana@example.com");
      catalogo.save("SWEB","Serviços Web",80);
      matriculas.save(1,1,SEMESTRE);
      avaliacoes.saveWithNotas(1,SEMESTRE,"Prova 1",1,
                               List.of(new NotaMatricula(1,9.0)));
      frequencias.save(LocalDate.of(2026,3,2),Map.of(1,true));

      disciplinas = new DisciplinaService(catalogo);
      relatorios  = new RelatorioService(alunos,catalogo,matriculas,avaliacoes,
                                         frequencias,
                                         new CalculadoraSituacao());
   }
}
