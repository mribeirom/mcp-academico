package br.edu.ifpr.mcp_academico.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_academico.domain.model.Aluno;
import br.edu.ifpr.mcp_academico.domain.model.Disciplina;
import br.edu.ifpr.mcp_academico.domain.model.Matricula;
import br.edu.ifpr.mcp_academico.domain.model.NotaAvaliacao;
import br.edu.ifpr.mcp_academico.domain.model.NotaMatricula;
import br.edu.ifpr.mcp_academico.domain.model.ResumoFrequencia;
import br.edu.ifpr.mcp_academico.domain.model.StatusMatricula;
import br.edu.ifpr.mcp_academico.persistence.AlunoRepository;
import br.edu.ifpr.mcp_academico.persistence.AvaliacaoRepository;
import br.edu.ifpr.mcp_academico.persistence.DisciplinaRepository;
import br.edu.ifpr.mcp_academico.persistence.FrequenciaRepository;
import br.edu.ifpr.mcp_academico.persistence.MatriculaRepository;

// dublês de teste: repositórios em memória escritos à mão, que permitem testar
// as regras de negócio sem banco de dados
public final class InMemoryRepositories {
   private InMemoryRepositories() {}

   public static class Alunos implements AlunoRepository {
      private final Map<Integer, Aluno> dados = new LinkedHashMap<>();

      @Override
      public Aluno save(String ra, String nome, String email) {
         Aluno aluno = new Aluno(dados.size() + 1,ra,nome,email);

         dados.put(aluno.id(),aluno);

         return aluno;
      }

      @Override
      public Optional<Aluno> findById(int id) {
         return Optional.ofNullable(dados.get(id));
      }

      @Override
      public boolean existsByRa(String ra) {
         return dados.values().stream().anyMatch(a -> a.ra().equals(ra));
      }

      @Override
      public boolean existsByEmail(String email) {
         return dados.values().stream().anyMatch(a -> a.email().equals(email));
      }

      @Override
      public List<Aluno> search(String termo, int limite) {
         return dados.values().stream()
                     .filter(a -> a.nome().contains(termo))
                     .limit(limite)
                     .toList();
      }
   }

   public static class Disciplinas implements DisciplinaRepository {
      private final Map<Integer, Disciplina> dados = new LinkedHashMap<>();

      @Override
      public Disciplina save(String codigo, String nome, int cargaHoraria) {
         Disciplina disciplina = new Disciplina(dados.size() + 1,codigo,nome,
                                                cargaHoraria,true);

         dados.put(disciplina.id(),disciplina);

         return disciplina;
      }

      public void inativar(int id) {
         Disciplina d = dados.get(id);

         dados.put(id,new Disciplina(d.id(),d.codigo(),d.nome(),
                                     d.cargaHoraria(),false));
      }

      @Override
      public Optional<Disciplina> findById(int id) {
         return Optional.ofNullable(dados.get(id));
      }

      @Override
      public boolean existsByCodigo(String codigo) {
         return dados.values().stream()
                     .anyMatch(d -> d.codigo().equals(codigo));
      }

      @Override
      public List<Disciplina> findAll(boolean somenteAtivas) {
         return dados.values().stream()
                     .filter(d -> !somenteAtivas || d.ativa())
                     .toList();
      }
   }

   public static class Matriculas implements MatriculaRepository {
      private final Map<Integer, Matricula> dados = new LinkedHashMap<>();

      @Override
      public Matricula save(int alunoId, int disciplinaId, String semestre) {
         Matricula matricula = new Matricula(dados.size() + 1,alunoId,
                                             disciplinaId,semestre,
                                             StatusMatricula.ATIVA);

         dados.put(matricula.id(),matricula);

         return matricula;
      }

      @Override
      public Optional<Matricula> findById(int id) {
         return Optional.ofNullable(dados.get(id));
      }

      @Override
      public Optional<Matricula> find(int alunoId, int disciplinaId,
                                      String semestre) {
         return dados.values().stream()
                     .filter(m -> m.alunoId() == alunoId &&
                                  m.disciplinaId() == disciplinaId &&
                                  m.semestre().equals(semestre))
                     .findFirst();
      }

      @Override
      public List<Matricula> findByAluno(int alunoId) {
         return dados.values().stream()
                     .filter(m -> m.alunoId() == alunoId)
                     .toList();
      }

      @Override
      public List<Matricula> findAtivasDaTurma(int disciplinaId,
                                               String semestre) {
         return dados.values().stream()
                     .filter(m -> m.disciplinaId() == disciplinaId &&
                                  m.semestre().equals(semestre) && m.ativa())
                     .toList();
      }

      @Override
      public void updateStatus(int id, StatusMatricula status) {
         Matricula m = dados.get(id);

         dados.put(id,new Matricula(m.id(),m.alunoId(),m.disciplinaId(),
                                    m.semestre(),status));
      }
   }

   public static class Avaliacoes implements AvaliacaoRepository {
      private final List<String>                     chaves = new ArrayList<>();
      private final Map<Integer, List<NotaAvaliacao>> notas = new HashMap<>();

      private String chave(int disciplinaId, String semestre, String descricao) {
         return disciplinaId + "|" + semestre + "|" + descricao;
      }

      @Override
      public boolean exists(int disciplinaId, String semestre,
                            String descricao) {
         return chaves.contains(chave(disciplinaId,semestre,descricao));
      }

      @Override
      public void saveWithNotas(int disciplinaId, String semestre,
                                String descricao, double peso,
                                List<NotaMatricula> itens) {
         chaves.add(chave(disciplinaId,semestre,descricao));
         itens.forEach(item -> notas.computeIfAbsent(item.matriculaId(),
                                                     k -> new ArrayList<>())
                                    .add(new NotaAvaliacao(descricao,peso,
                                                           item.valor())));
      }

      @Override
      public List<NotaAvaliacao> findNotas(int matriculaId) {
         return notas.getOrDefault(matriculaId,List.of());
      }
   }

   public static class Frequencias implements FrequenciaRepository {
      private final Map<Integer, Map<LocalDate, Boolean>> dados = new HashMap<>();

      @Override
      public void save(LocalDate data, Map<Integer, Boolean> presencas) {
         presencas.forEach((matriculaId,presente) ->
               dados.computeIfAbsent(matriculaId,k -> new HashMap<>())
                    .put(data,presente));
      }

      @Override
      public ResumoFrequencia resumo(int matriculaId) {
         Map<LocalDate, Boolean> aulas = dados.getOrDefault(matriculaId,
                                                            Map.of());

         return new ResumoFrequencia(aulas.size(),
                                     (int) aulas.values().stream()
                                                .filter(p -> p).count());
      }
   }
}
