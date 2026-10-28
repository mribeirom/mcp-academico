-- dados fictícios que cobrem todas as situações acadêmicas:
-- aprovado, reprovado por nota, reprovado por frequência, em curso e cancelada

INSERT INTO aluno (id, ra, nome, email) VALUES
   (1, '2024001', 'Ana Luíza Gonçalves',  'ana.goncalves@example.com'),
   (2, '2024002', 'João Pedro Conceição', 'joao.conceicao@example.com'),
   (3, '2024003', 'Maria Antônia Araújo', 'maria.araujo@example.com'),
   (4, '2024004', 'Luís Fernando Müller', 'luis.muller@example.com'),
   (5, '2024005', 'Beatriz Simões',       'beatriz.simoes@example.com'),
   (6, '2024006', 'Caio Assunção',        'caio.assuncao@example.com'),
   (7, '2024007', 'Débora Nóbrega',       'debora.nobrega@example.com'),
   (8, '2024008', 'Érico Magalhães',      'erico.magalhaes@example.com');

INSERT INTO disciplina (id, codigo, nome, carga_horaria, ativa) VALUES
   (1, 'SWEB',  'Serviços Web',                    80,  1),
   (2, 'POO',   'Programação Orientada a Objetos', 120, 1),
   (3, 'BD',    'Banco de Dados',                  80,  1),
   (4, 'LEGIS', 'Legislação em Informática',       40,  0);

INSERT INTO matricula (id, aluno_id, disciplina_id, semestre, status) VALUES
   -- Serviços Web em 2026/1: semestre com todos os lançamentos realizados
   (1,  1, 1, '2026/1', 'ATIVA'),
   (2,  2, 1, '2026/1', 'ATIVA'),
   (3,  3, 1, '2026/1', 'ATIVA'),
   (4,  4, 1, '2026/1', 'ATIVA'),
   (5,  5, 1, '2026/1', 'ATIVA'),
   (6,  6, 1, '2026/1', 'CANCELADA'),
   -- Programação Orientada a Objetos em 2026/1
   (7,  1, 2, '2026/1', 'ATIVA'),
   (8,  2, 2, '2026/1', 'ATIVA'),
   -- 2026/2: semestre em andamento
   (9,  1, 1, '2026/2', 'ATIVA'),
   (10, 7, 1, '2026/2', 'ATIVA'),
   (11, 8, 1, '2026/2', 'ATIVA'),
   (12, 1, 3, '2026/2', 'ATIVA');

INSERT INTO avaliacao (id, disciplina_id, semestre, descricao, peso) VALUES
   (1, 1, '2026/1', 'Prova 1',       4),
   (2, 1, '2026/1', 'Prova 2',       6),
   (3, 2, '2026/1', 'Projeto final', 10),
   (4, 1, '2026/2', 'Prova 1',       5);

INSERT INTO nota (avaliacao_id, matricula_id, valor) VALUES
   (1, 1, 8.0), (2, 1, 9.0),
   (1, 2, 7.0), (2, 2, 6.5),
   (1, 3, 4.0), (2, 3, 5.0),
   (1, 4, 9.0), (2, 4, 8.5),
   (1, 5, 6.0), (2, 5, 7.0),
   (3, 7, 9.5),
   (3, 8, 5.5),
   (4, 9, 8.5);

INSERT INTO frequencia (matricula_id, data, presente) VALUES
   (1, '2026-03-02', 1), (1, '2026-03-09', 1), (1, '2026-03-16', 1), (1, '2026-03-23', 1),
   (2, '2026-03-02', 1), (2, '2026-03-09', 1), (2, '2026-03-16', 1), (2, '2026-03-23', 0),
   (3, '2026-03-02', 1), (3, '2026-03-09', 1), (3, '2026-03-16', 1), (3, '2026-03-23', 1),
   (4, '2026-03-02', 1), (4, '2026-03-09', 1), (4, '2026-03-16', 0), (4, '2026-03-23', 0),
   (5, '2026-03-02', 1), (5, '2026-03-09', 1), (5, '2026-03-16', 1), (5, '2026-03-23', 1),
   (7, '2026-03-03', 1), (7, '2026-03-10', 1),
   (8, '2026-03-03', 1), (8, '2026-03-10', 1);
