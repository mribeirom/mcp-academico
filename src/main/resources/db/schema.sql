CREATE TABLE IF NOT EXISTS aluno (
   id    INTEGER PRIMARY KEY AUTOINCREMENT,
   ra    TEXT NOT NULL UNIQUE,
   nome  TEXT NOT NULL,
   email TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS disciplina (
   id            INTEGER PRIMARY KEY AUTOINCREMENT,
   codigo        TEXT    NOT NULL UNIQUE,
   nome          TEXT    NOT NULL,
   carga_horaria INTEGER NOT NULL CHECK (carga_horaria > 0),
   ativa         INTEGER NOT NULL DEFAULT 1 CHECK (ativa IN (0, 1))
);

CREATE TABLE IF NOT EXISTS matricula (
   id            INTEGER PRIMARY KEY AUTOINCREMENT,
   aluno_id      INTEGER NOT NULL REFERENCES aluno (id),
   disciplina_id INTEGER NOT NULL REFERENCES disciplina (id),
   semestre      TEXT    NOT NULL,
   status        TEXT    NOT NULL CHECK (status IN ('ATIVA', 'CANCELADA')),
   UNIQUE (aluno_id, disciplina_id, semestre)
);

CREATE TABLE IF NOT EXISTS avaliacao (
   id            INTEGER PRIMARY KEY AUTOINCREMENT,
   disciplina_id INTEGER NOT NULL REFERENCES disciplina (id),
   semestre      TEXT    NOT NULL,
   descricao     TEXT    NOT NULL,
   peso          REAL    NOT NULL CHECK (peso > 0),
   UNIQUE (disciplina_id, semestre, descricao)
);

CREATE TABLE IF NOT EXISTS nota (
   avaliacao_id INTEGER NOT NULL REFERENCES avaliacao (id),
   matricula_id INTEGER NOT NULL REFERENCES matricula (id),
   valor        REAL    NOT NULL CHECK (valor BETWEEN 0 AND 10),
   PRIMARY KEY (avaliacao_id, matricula_id)
);

CREATE TABLE IF NOT EXISTS frequencia (
   matricula_id INTEGER NOT NULL REFERENCES matricula (id),
   data         TEXT    NOT NULL,
   presente     INTEGER NOT NULL CHECK (presente IN (0, 1)),
   PRIMARY KEY (matricula_id, data)
);
