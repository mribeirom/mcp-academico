<!--
Markdown Cheatsheet
https://github.com/adam-p/markdown-here/wiki/markdown-cheatsheet
-->

## MCP Acadêmico 

Servidor e cliente MCP para gestão acadêmica: alunos, disciplinas, matrículas, notas e frequência.

Um único projeto Maven gera duas aplicações:

- **Servidor MCP:** disponibiliza 12 ferramentas, 2 resources, 1 resource template e 3 prompts, usando o transporte STDIO.
- **Cliente MCP:** aplicação de console que executa toda e qualquer ferramenta do servidor, pois monta suas perguntas a partir do esquema JSON de cada ferramenta.

###  Requisitos 

| Software | Versão | Uso |
| --- | --- | --- |
| JDK | 25 | Compilar e executar as aplicações |
| Apache Maven | 3.9 ou superior | Linha de comando. O Eclipse IDE já inclui o Maven |
| Docker Desktop | Atual | Para o MCP Inspector em contêiner |


---

### Gerar as aplicações

Todos os comandos devem ser executados no diretório raiz do projeto.

| Objetivo | Comando |
| --- | --- |
| Testes unitários | `mvn clean test` |
| Testes unitários e de integração | `mvn clean verify -Pserver` |
| Gerar o servidor | `mvn clean package -Pserver -DskipTests` |
| Gerar o cliente | `mvn clean package -Pclient -DskipTests` |

O perfil `server` cria o diretório **server**, com o arquivo `mcp_academico_server.jar` e as bibliotecas em **libs**. O perfil `client` cria o diretório **target/dist-client**, com o arquivo `mcp_academico_client.jar` e as bibliotecas em **libs**.

**OBSERVAÇÕES IMPORTANTES**

**Gere primeiro o servidor e depois o cliente.** O objetivo (*goal*) `clean` apaga o diretório **target**, então gerar o servidor apaga o cliente, que deve ser gerado novamente mesmo sem alteração no código.

**Encerre o Claude Desktop e o MCP Inspector antes de gerar o servidor.** No Windows, um servidor em execução bloqueia o arquivo JAR, e o `clean` não consegue apagar o diretório **server**.

---



### Executar o cliente MCP 

Depois de gerar as duas aplicações, nessa ordem:

```
cd target/dist-client
java -jar mcp_academico_client.jar
```

O cliente executa o servidor, sem intervenção do usuário, e apresenta o menu:

```
 1 - Ferramentas
 2 - Resources
 3 - Prompts
 sair - encerrar o cliente
```

Ao escolher uma ferramenta, pelo número ou pelo nome, o cliente solicita os parâmetros de acordo com o tipo de cada um:

| Tipo do parâmetro | Como informar |
| --- | --- |
| Texto, número ou inteiro | O próprio valor. Números aceitam ponto ou vírgula |
| Booleano | `sim`, `não`, `true` ou `false` |
| Enumeração | Um dos valores exibidos |
| Lista de valores | Itens separados por vírgulas, ou JSON como `[1,2]`. Use `[]` para uma lista vazia |
| Lista de objetos | O cliente pergunta se deseja adicionar um item e solicita seus campos |
| Qualquer outro tipo | Valor em JSON |

Parâmetros opcionais podem ser deixados em branco.

O servidor é localizado a partir do diretório do cliente. Para usar outro arquivo, informe seu caminho na variável de ambiente `MCP_SERVER_JAR`.

---

### Instalar no Claude Desktop 

1. Gerar o servidor
2. Localizar o diretório `C:\Users\<USUÁRIO_LOGADO>\AppData\Local\Packages\Claude_pzs8sxrjxfjjc\LocalCache\Roaming\Claude`. O nome `Claude_pzs8sxrjxfjjc` pode ser diferente, mas o prefixo `Claude_` será o mesmo
3. Editar o arquivo `claude_desktop_config.json`, criando a entrada `mcpServers` se ela não existir:

```json
{
  "mcpServers": {
    "mcp-academico": {
      "command": "java",
      "args": ["-jar", "C:/caminho/absoluto/para/server/mcp_academico_server.jar"],
      "env": {}
    }
  }
}
```

4. Salvar o arquivo e encerrar completamente o Claude Desktop, e não apenas fechar sua janela
5. Abrir o Claude Desktop e testar com um pedido como: *"Mostre o boletim da aluna Ana Luíza em 2026/1"*

Se o `java` do sistema não for a versão 25, troque `"java"` pelo caminho completo, como `"C:/Program Files/Eclipse Adoptium/jdk-25/bin/java.exe"`.

---


#### Docker Desktop 

1. Gerar o servidor
2. Certificar-se de que o Docker Desktop esteja em execução
3. Executar `docker compose up -d --build` no diretório raiz do projeto
4. Acessar `http://127.0.0.1:6274` no navegador

O arquivo `compose.yaml` processa o `Dockerfile`, que cria a imagem com o Inspector e o Java 25. O diretório **server** é montado somente para leitura, e o banco de dados fica em um volume próprio, preservado entre as execuções.

### Ferramentas 

O resultado é retornado como texto e como conteúdo estruturado (`structuredContent`). Os erros de cada ferramenta estão descritos na seção Erros. Parâmetros sem a indicação *opcional* são obrigatórios.

Formatos usados nos parâmetros: `semestre` é `AAAA/S`, como `2026/2`, e `data` é `AAAA-MM-DD`, como `2026-03-02`.

#### `cadastrar_aluno`

Cadastra um novo aluno.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `nome` | texto | Nome completo |
| `email` | texto | E-mail, gravado em minúsculas |
| `ra` | texto | Registro acadêmico |

**Resultado:** `{id, ra, nome, email}`

**Erros:** `EMAIL_INVALIDO`, `ALUNO_DUPLICADO`

#### `buscar_alunos`

Busca alunos por trecho do nome, do RA ou do e-mail, em ordem alfabética.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `termo` | texto, opcional | Trecho procurado. Sem ele, lista todos os alunos |
| `limite` | inteiro, opcional | Máximo de alunos, de 1 a 100. Padrão: 20 |

**Resultado:** `{alunos: [{id, ra, nome, email}]}`. A lista pode ser vazia.

**Erros:** `LIMITE_INVALIDO`

#### `cadastrar_disciplina`

Cadastra uma nova disciplina, que nasce ativa.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `codigo` | texto | Código único, gravado em maiúsculas |
| `nome` | texto | Nome da disciplina |
| `carga_horaria` | inteiro | De 1 a 1000 horas |

**Resultado:** `{id, codigo, nome, carga_horaria, ativa}`

**Erros:** `CARGA_INVALIDA`, `DISCIPLINA_DUPLICADA`

#### `listar_disciplinas`

Lista as disciplinas em ordem alfabética.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `somente_ativas` | booleano, opcional | Exclui as disciplinas inativas. Padrão: `false` |

**Resultado:** `{disciplinas: [{id, codigo, nome, carga_horaria, ativa}]}`

**Erros:** nenhum erro de negócio.

#### `matricular_aluno`

Matricula um aluno em uma disciplina ativa. Uma matrícula cancelada é reativada.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `aluno_id` | inteiro | ID do aluno |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da matrícula |

**Resultado:** `{id, aluno_id, disciplina_id, semestre, status}`, com `status` igual a `ATIVA`.

**Erros:** `SEMESTRE_INVALIDO`, `ALUNO_NAO_ENCONTRADO`, `DISCIPLINA_NAO_ENCONTRADA`, `DISCIPLINA_INATIVA`, `MATRICULA_DUPLICADA`

#### `cancelar_matricula`

Cancela uma matrícula ativa. A ferramenta é anunciada como destrutiva, então os assistentes de IA podem pedir confirmação.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `matricula_id` | inteiro | ID da matrícula |

**Resultado:** `{id, aluno_id, disciplina_id, semestre, status}`, com `status` igual a `CANCELADA`.

**Erros:** `MATRICULA_NAO_ENCONTRADA`, `MATRICULA_JA_CANCELADA`

#### `lancar_notas`

Lança as notas de uma avaliação. O lançamento é uma transação: se alguma nota for inválida, nenhuma é gravada.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da turma |
| `avaliacao` | texto | Nome da avaliação, único na turma |
| `peso` | número | Peso da avaliação, maior que zero |
| `notas` | lista de objetos | Itens `{aluno_id, valor}`, com `valor` de 0 a 10 |

**Resultado:** `{notas_lancadas}`

**Erros:** `SEMESTRE_INVALIDO`, `DISCIPLINA_NAO_ENCONTRADA`, `PESO_INVALIDO`, `NOTA_FORA_DA_FAIXA`, `AVALIACAO_DUPLICADA`, `ALUNO_NAO_MATRICULADO`, `ARGUMENTO_INVALIDO` (lista vazia ou aluno repetido)

#### `registrar_frequencia`

Registra a frequência de uma aula: os alunos não informados como ausentes recebem presença. Repetir a data substitui o registro anterior.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da turma |
| `data` | texto | Data da aula, que não pode ser futura |
| `ausentes` | lista de inteiros | IDs dos alunos ausentes. Pode ser vazia |

**Resultado:** `{alunos_registrados}`

**Erros:** `SEMESTRE_INVALIDO`, `DATA_INVALIDA`, `DISCIPLINA_NAO_ENCONTRADA`, `TURMA_VAZIA`, `ALUNO_NAO_MATRICULADO`

#### `calcular_situacao`

Calcula a média ponderada, a frequência e a situação de um aluno em uma disciplina.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `aluno_id` | inteiro | ID do aluno |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da matrícula |

**Resultado:** `{media, frequencia, situacao}`. `media` e `frequencia` são nulas enquanto não houver lançamentos.

**Erros:** `SEMESTRE_INVALIDO`, `MATRICULA_NAO_ENCONTRADA`

#### `gerar_boletim`

Gera o boletim de um aluno em um semestre.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `aluno_id` | inteiro | ID do aluno |
| `semestre` | texto | Semestre do boletim |
| `formato` | enumeração | `texto` ou `json`, que define o conteúdo textual do resultado |

**Resultado:** o conteúdo estruturado é sempre `{aluno, semestre, disciplinas: [{codigo, disciplina, semestre, notas, resultado}]}`.

**Erros:** `SEMESTRE_INVALIDO`, `ALUNO_NAO_ENCONTRADO`, `SEM_MATRICULAS`

#### `ranking_turma`

Classifica os alunos de uma turma pela média, da maior para a menor, com desempate pelo nome. Alunos sem notas não são classificados.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da turma |
| `limite` | inteiro, opcional | Tamanho do ranking, de 1 a 100. Padrão: 10 |

**Resultado:** `{ranking: [{posicao, aluno_id, nome, media, situacao}]}`

**Erros:** `LIMITE_INVALIDO`, `SEMESTRE_INVALIDO`, `DISCIPLINA_NAO_ENCONTRADA`, `TURMA_VAZIA`

#### `estatisticas_disciplina`

Calcula as estatísticas de uma turma.

| Parâmetro | Tipo | Descrição |
| --- | --- | --- |
| `disciplina_id` | inteiro | ID da disciplina |
| `semestre` | texto | Semestre da turma |

**Resultado:** `{alunos, avaliados, media, maior_media, menor_media, desvio_padrao, aprovados, reprovados_nota, reprovados_frequencia, em_curso, taxa_aprovacao}`. Os valores calculados são nulos enquanto não houver lançamentos.

**Erros:** `SEMESTRE_INVALIDO`, `DISCIPLINA_NAO_ENCONTRADA`, `TURMA_VAZIA`

---

### Resources 

| URI | Tipo | Conteúdo |
| --- | --- | --- |
| `academico://disciplinas` | `application/json` | Catálogo com todas as disciplinas |
| `academico://regras/aprovacao` | `text/markdown` | Critérios de aprovação e situações possíveis |
| `academico://alunos/{id}/historico` | `application/json` | Resource template com o histórico do aluno de ID informado |

**Erros do template:** `ARGUMENTO_INVALIDO` (ID não numérico) e `ALUNO_NAO_ENCONTRADO`.

---

### Prompts

Os prompts geram uma mensagem com instruções ao assistente de IA, incluindo os dados do banco. Todos os argumentos são obrigatórios.

| Prompt | Argumentos | Objetivo | Erros |
| --- | --- | --- | --- |
| `analisar_desempenho` | `aluno_id`, `semestre` | Análise do boletim do aluno | `ALUNO_NAO_ENCONTRADO`, `SEM_MATRICULAS` |
| `plano_recuperacao` | `aluno_id`, `disciplina_id`, `semestre` | Plano de quatro semanas para o aluno | `MATRICULA_NAO_ENCONTRADA` |
| `relatorio_turma` | `disciplina_id`, `semestre` | Relatório do desempenho da turma | `DISCIPLINA_NAO_ENCONTRADA`, `TURMA_VAZIA` |

Todos também podem retornar `ARGUMENTO_INVALIDO` e `SEMESTRE_INVALIDO`.

---

### Erros

Uma ferramenta nunca interrompe a conversa com o assistente de IA: seus erros são retornados como resultado, com `isError` verdadeiro, para que a IA possa corrigir a chamada. O texto traz o código entre colchetes, e o conteúdo estruturado traz os mesmos dados:

```json
{"codigo": "NOTA_FORA_DA_FAIXA", "mensagem": "As notas devem estar entre 0 e 10."}
```

Argumentos fora do esquema, como texto em um parâmetro inteiro, são rejeitados pelo SDK antes da execução da ferramenta, também com `isError` verdadeiro. Resources e prompts não possuem `isError`, então seus erros são retornados como erros do protocolo, com o mesmo código na mensagem.

| Código | Significado |
| --- | --- |
| `ARGUMENTO_INVALIDO` | Argumento ausente, em branco ou de tipo incorreto |
| `LIMITE_INVALIDO` | Limite fora da faixa de 1 a 100 |
| `SEMESTRE_INVALIDO` | Semestre fora do formato `AAAA/S`, com `S` igual a 1 ou 2 |
| `DATA_INVALIDA` | Data em formato incorreto, inexistente ou futura |
| `EMAIL_INVALIDO` | E-mail em formato incorreto |
| `ALUNO_DUPLICADO` | RA ou e-mail já cadastrado |
| `ALUNO_NAO_ENCONTRADO` | Não existe aluno com o ID informado |
| `CARGA_INVALIDA` | Carga horária fora da faixa de 1 a 1000 |
| `DISCIPLINA_DUPLICADA` | Código de disciplina já cadastrado |
| `DISCIPLINA_NAO_ENCONTRADA` | Não existe disciplina com o ID informado |
| `DISCIPLINA_INATIVA` | A disciplina não aceita matrículas |
| `MATRICULA_DUPLICADA` | O aluno já está matriculado na turma |
| `MATRICULA_NAO_ENCONTRADA` | Matrícula inexistente |
| `MATRICULA_JA_CANCELADA` | A matrícula já foi cancelada |
| `ALUNO_NAO_MATRICULADO` | O aluno não possui matrícula ativa na turma |
| `SEM_MATRICULAS` | O aluno não possui matrículas no semestre |
| `TURMA_VAZIA` | A turma não possui alunos matriculados |
| `PESO_INVALIDO` | O peso da avaliação não é positivo |
| `NOTA_FORA_DA_FAIXA` | Nota fora da faixa de 0 a 10 |
| `AVALIACAO_DUPLICADA` | A avaliação já foi lançada na turma |
| `ERRO_INTERNO` | Falha não prevista, registrada no log do servidor |

---

### Regras de aprovação

O aluno é aprovado com **média ponderada igual ou superior a 6,0** e **frequência igual ou superior a 75%**.

| Situação | Quando ocorre |
| --- | --- |
| `EM_CURSO` | Ainda não há notas ou frequência registradas |
| `APROVADO` | Atende aos dois critérios |
| `REPROVADO_NOTA` | Frequência suficiente e média insuficiente |
| `REPROVADO_FREQUENCIA` | Frequência insuficiente, qualquer que seja a média |
| `CANCELADA` | A matrícula foi cancelada |

---

### Banco de dados

Os dados ficam em um arquivo SQLite, acessado com JDBC puro. Nada precisa ser instalado: o banco é criado na primeira execução com dados fictícios, que cobrem todas as situações acadêmicas.

- **Local padrão:** `.mcp-academico/academico.db`, no diretório do usuário
- **Outro local:** informe o caminho na variável de ambiente `ACADEMICO_DB_PATH`
- **Recomeçar com os dados de exemplo:** encerre o servidor e apague o diretório `.mcp-academico`

O caminho não depende do diretório de trabalho, que é diferente no cliente, no Claude Desktop e no MCP Inspector. O modo WAL do SQLite permite usar o cliente com o Claude Desktop aberto, compartilhando os mesmos dados.

Dados de exemplo úteis para os testes:

| Dado | Valor |
| --- | --- |
| Alunos | IDs de 1 a 8 |
| Disciplinas | 1 Serviços Web, 2 Programação Orientada a Objetos, 3 Banco de Dados, 4 Legislação (inativa) |
| Semestre completo | `2026/1`, com Serviços Web lançada para os alunos de 1 a 5 |
| Semestre em andamento | `2026/2` |

---

### Arquitetura 

```
br.edu.ifpr.mcp_academico
├── server            ponto de entrada e composição dos objetos (ServerFeatures)
│   ├── tools         uma classe por ferramenta
│   ├── resources     uma classe por resource
│   ├── prompts       uma classe por prompt
│   └── support       AbstractTool, AbstractPrompt, leitura de argumentos e resultados
├── domain
│   ├── model         records e enumerações
│   ├── service       regras de negócio
│   └── exception     DomainException e códigos de erro
├── persistence       interfaces dos repositórios
│   └── jdbc          implementações com JDBC puro
└── client            ponto de entrada, conexão, console e resultados
    ├── menu          menu principal e suas ações
    └── arguments     leitura de argumentos a partir do esquema JSON
```


### Testes 

```
mvn clean verify -Pserver
```

- **Testes unitários** (sufixo `Test`): cobrem os retornos e erros dos métodos. As regras de negócio usam repositórios em memória, e os repositórios JDBC usam um SQLite real em arquivo temporário.
- **Testes de integração** (sufixo `IT`): executam o servidor real via STDIO, com um banco temporário.

| Classe | O que verifica |
| --- | --- |
| `FerramentasIT` | Sucesso e erros de todas as ferramentas |
| `McpAcademicoServerIT` | Capacidades, resources, template e prompts |
| `ClienteIT` | O menu do cliente digitando como um usuário |
| `ConcorrenciaIT` | Dois servidores criando e gravando no mesmo banco ao mesmo tempo |

O relatório de cobertura fica em `target/site/jacoco/index.html`. O build falha se a cobertura de linhas ficar abaixo de 90%. O servidor dos testes de integração é executado em outro processo, então a cobertura medida vem principalmente dos testes unitários.


