/* Cria o banco apenas se ainda não existir. */
CREATE DATABASE banco_girassol;
GO

USE banco_girassol;
GO

/* Tabela principal de pessoas (responsáveis, professores e pessoas autorizadas). */
CREATE TABLE dbo.pessoas (
    codigo INT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(50) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    rg VARCHAR(9) NULL,
    data_nascimento DATE NOT NULL,
    senha VARCHAR(100) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    endereco VARCHAR(100) NOT NULL,
    telefone VARCHAR(15) NOT NULL,

    CONSTRAINT PK_pessoas PRIMARY KEY (codigo),
    CONSTRAINT UQ_pessoas_cpf UNIQUE (cpf),
    CONSTRAINT UQ_pessoas_rg UNIQUE (rg)
);
GO

/* Alunos. */
CREATE TABLE dbo.alunos (
    codigo INT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    alergias VARCHAR(50) NOT NULL,
    restricoes_alimentar VARCHAR(50) NOT NULL,
    necessidades_especiais VARCHAR(MAX) NULL,

    CONSTRAINT PK_alunos PRIMARY KEY (codigo)
);
GO

/* Professores: o código da pessoa também é o identificador do professor. */
CREATE TABLE dbo.professores (
    pessoa_codigo INT NOT NULL,
    data_contratacao VARCHAR(50) NOT NULL,
    formacao VARCHAR(50) NOT NULL,
    status INT NOT NULL,

    CONSTRAINT PK_professores PRIMARY KEY (pessoa_codigo),
    CONSTRAINT FK_professores_pessoas FOREIGN KEY (pessoa_codigo)
        REFERENCES dbo.pessoas (codigo)
);
GO

/* Responsáveis: o código da pessoa também é o identificador do responsável. */
CREATE TABLE dbo.responsaveis (
    pessoa_codigo INT NOT NULL,
    local_trabalho VARCHAR(50) NOT NULL,
    telefone_trabalho VARCHAR(15) NOT NULL,
    estado_civil VARCHAR(50) NOT NULL,
    status INT NOT NULL,

    CONSTRAINT PK_responsaveis PRIMARY KEY (pessoa_codigo),
    CONSTRAINT FK_responsaveis_pessoas FOREIGN KEY (pessoa_codigo)
        REFERENCES dbo.pessoas (codigo)
);
GO

/* Pessoas autorizadas a buscar alunos. */
CREATE TABLE dbo.autorizados_busca (
    codigo INT NOT NULL,
    aluno_codigo INT NOT NULL,
    grau_parentesco VARCHAR(50) NOT NULL,

    CONSTRAINT PK_autorizados_busca PRIMARY KEY (codigo, aluno_codigo),
    CONSTRAINT FK_autorizados_busca_pessoas FOREIGN KEY (codigo)
        REFERENCES dbo.pessoas (codigo),
    CONSTRAINT FK_autorizados_busca_alunos FOREIGN KEY (aluno_codigo)
        REFERENCES dbo.alunos (codigo)
);
GO

/* Turmas. */
CREATE TABLE dbo.turmas (
    codigo INT IDENTITY(1,1) NOT NULL,
    professor_codigo INT NOT NULL,
    nome_turma VARCHAR(50) NOT NULL,
    ano VARCHAR(50) NOT NULL,
    grau VARCHAR(50) NOT NULL,

    CONSTRAINT PK_turmas PRIMARY KEY (codigo),
    CONSTRAINT FK_turmas_professores FOREIGN KEY (professor_codigo)
        REFERENCES dbo.professores (pessoa_codigo)
);
GO

/* Relação entre responsáveis e alunos. */
CREATE TABLE dbo.responsavel_aluno (
    responsavel_codigo INT NOT NULL,
    aluno_codigo INT NOT NULL,
    grau_parentesco VARCHAR(20) NOT NULL,
    esp_financeiro BIT NOT NULL,
    ordem_contato INT NOT NULL,

    CONSTRAINT PK_responsavel_aluno PRIMARY KEY (responsavel_codigo, aluno_codigo),
    CONSTRAINT FK_responsavel_aluno_responsaveis FOREIGN KEY (responsavel_codigo)
        REFERENCES dbo.responsaveis (pessoa_codigo),
    CONSTRAINT FK_responsavel_aluno_alunos FOREIGN KEY (aluno_codigo)
        REFERENCES dbo.alunos (codigo)
);
GO

/* Matrículas. */
CREATE TABLE dbo.matriculas (
    nr INT IDENTITY(1,1) NOT NULL,
    aluno_codigo INT NOT NULL,
    turma_codigo INT NOT NULL,
    data_matricula VARCHAR(50) NOT NULL,
    status INT NOT NULL,

    CONSTRAINT PK_matriculas PRIMARY KEY (nr),
    CONSTRAINT FK_matriculas_alunos FOREIGN KEY (aluno_codigo)
        REFERENCES dbo.alunos (codigo),
    CONSTRAINT FK_matriculas_turmas FOREIGN KEY (turma_codigo)
        REFERENCES dbo.turmas (codigo)
);
GO

/* Planejamento de aulas. */
CREATE TABLE dbo.aulas_planejamentos (
    codigo INT IDENTITY(1,1) NOT NULL,
    professor_codigo INT NOT NULL,
    turma_codigo INT NOT NULL,
    data_aula DATE NOT NULL,
    atividade_dinamica VARCHAR(200) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    status INT NOT NULL,

    CONSTRAINT PK_aulas_planejamentos PRIMARY KEY (codigo),
    CONSTRAINT FK_aulas_planejamentos_professores FOREIGN KEY (professor_codigo)
        REFERENCES dbo.professores (pessoa_codigo),
    CONSTRAINT FK_aulas_planejamentos_turmas FOREIGN KEY (turma_codigo)
        REFERENCES dbo.turmas (codigo)
);
GO

/* Diário de bordo. */
CREATE TABLE dbo.diario_bordo (
    codigo INT IDENTITY(1,1) NOT NULL,
    matricula_nr INT NOT NULL,
    data_registro VARCHAR(50) NOT NULL,
    compareceu BIT NOT NULL,
    alimentacao VARCHAR(50) NULL,
    sono VARCHAR(50) NULL,
    humor VARCHAR(50) NULL,
    banheiro_fralda VARCHAR(50) NULL,
    observacoes VARCHAR(500) NULL,

    CONSTRAINT PK_diario_bordo PRIMARY KEY (codigo),
    CONSTRAINT FK_diario_bordo_matriculas FOREIGN KEY (matricula_nr)
        REFERENCES dbo.matriculas (nr)
);
GO

/* Frequência: chave primária composta por aula e matrícula. */
CREATE TABLE dbo.frequencias (
    aula_planejamentos_codigo INT NOT NULL,
    matricula_codigo INT NOT NULL,
    status_presenca VARCHAR(1) NOT NULL,

    CONSTRAINT PK_frequencias PRIMARY KEY (aula_planejamentos_codigo, matricula_codigo),
    CONSTRAINT FK_frequencias_aulas_planejamentos FOREIGN KEY (aula_planejamentos_codigo)
        REFERENCES dbo.aulas_planejamentos (codigo),
    CONSTRAINT FK_frequencias_matriculas FOREIGN KEY (matricula_codigo)
        REFERENCES dbo.matriculas (nr)
);
GO
