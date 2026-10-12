CREATE DATABASE banco_girassol;
GO USE banco_girassol;
GO --------------------------------------------------------------------------------
    -- 1. TABELA DE PESSOAS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.pessoas (
        codigo INT IDENTITY(1, 1) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL,
        cpf VARCHAR(11) NOT NULL,
        rg VARCHAR(20) NULL,
        data_nascimento DATE NOT NULL,
        senha VARCHAR(255) NULL,
        tipo_perfil VARCHAR(20) NOT NULL CONSTRAINT DF_pessoas_perfil DEFAULT 'RESPONSAVEL',
        cep VARCHAR(8) NOT NULL,
        cidade VARCHAR(50) NOT NULL,
        uf CHAR(2) NOT NULL,
        endereco VARCHAR(100) NOT NULL,
        telefone VARCHAR(15) NOT NULL,
        CONSTRAINT PK_pessoas PRIMARY KEY (codigo),
        CONSTRAINT UQ_pessoas_cpf UNIQUE (cpf),
        CONSTRAINT UQ_pessoas_email UNIQUE (email)
    );
GO --------------------------------------------------------------------------------
    -- 2. TABELA DE ALUNOS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.alunos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        data_nascimento DATE NOT NULL,
        alergias VARCHAR(100) NOT NULL,
        restricoes_alimentar VARCHAR(100) NOT NULL,
        necessidades_especiais VARCHAR(MAX) NULL,
        CONSTRAINT PK_alunos PRIMARY KEY (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 3. TABELA DE PROFESSORES
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.professores (
        pessoa_codigo INT NOT NULL,
        data_contratacao DATE NOT NULL,
        formacao VARCHAR(100) NOT NULL,
        status INT NOT NULL CONSTRAINT DF_professores_status DEFAULT 1,
        CONSTRAINT PK_professores PRIMARY KEY (pessoa_codigo),
        CONSTRAINT FK_professores_pessoas FOREIGN KEY (pessoa_codigo) REFERENCES dbo.pessoas (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 4. TABELA DE RESPONSÁVEIS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.responsaveis (
        pessoa_codigo INT NOT NULL,
        local_trabalho VARCHAR(100) NOT NULL,
        telefone_trabalho VARCHAR(15) NOT NULL,
        estado_civil VARCHAR(50) NOT NULL,
        status INT NOT NULL CONSTRAINT DF_responsaveis_status DEFAULT 1,
        CONSTRAINT PK_responsaveis PRIMARY KEY (pessoa_codigo),
        CONSTRAINT FK_responsaveis_pessoas FOREIGN KEY (pessoa_codigo) REFERENCES dbo.pessoas (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 5. TABELA DE PESSOAS AUTORIZADAS A BUSCAR
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.autorizados_busca (
        codigo INT NOT NULL,
        aluno_codigo INT NOT NULL,
        responsavel_codigo INT NOT NULL,
        grau_parentesco VARCHAR(50) NOT NULL,
        CONSTRAINT PK_autorizados_busca PRIMARY KEY (codigo, aluno_codigo, responsavel_codigo),
        CONSTRAINT FK_autorizados_busca_pessoas FOREIGN KEY (codigo) REFERENCES dbo.pessoas (codigo),
        CONSTRAINT FK_autorizados_busca_alunos FOREIGN KEY (aluno_codigo) REFERENCES dbo.alunos (codigo),
        CONSTRAINT FK_autorizados_busca_responsaveis FOREIGN KEY (responsavel_codigo) REFERENCES dbo.responsaveis (pessoa_codigo)
    );
GO --------------------------------------------------------------------------------
    -- 6. TABELA DE TURMAS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.turmas (
        codigo INT IDENTITY(1, 1) NOT NULL,
        professor_codigo INT NOT NULL,
        nome_turma VARCHAR(50) NOT NULL,
        ano INT NOT NULL,
        grau VARCHAR(50) NOT NULL,
        CONSTRAINT PK_turmas PRIMARY KEY (codigo),
        CONSTRAINT FK_turmas_professores FOREIGN KEY (professor_codigo) REFERENCES dbo.professores (pessoa_codigo)
    );
GO --------------------------------------------------------------------------------
    -- 7. RELACIONAMENTO RESPONSÁVEL <-> ALUNO
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.responsavel_aluno (
        responsavel_codigo INT NOT NULL,
        aluno_codigo INT NOT NULL,
        grau_parentesco VARCHAR(20) NOT NULL,
        esp_financeiro BIT NOT NULL,
        ordem_contato INT NOT NULL,
        CONSTRAINT PK_responsavel_aluno PRIMARY KEY (responsavel_codigo, aluno_codigo),
        CONSTRAINT FK_responsavel_aluno_responsaveis FOREIGN KEY (responsavel_codigo) REFERENCES dbo.responsaveis (pessoa_codigo),
        CONSTRAINT FK_responsavel_aluno_alunos FOREIGN KEY (aluno_codigo) REFERENCES dbo.alunos (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 8. TABELA DE MATRÍCULAS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.matriculas (
        nr INT IDENTITY(1, 1) NOT NULL,
        aluno_codigo INT NOT NULL,
        turma_codigo INT NOT NULL,
        data_envio DATETIME NOT NULL CONSTRAINT DF_matriculas_envio DEFAULT GETDATE(),
        data_decisao DATETIME NULL,
        status INT NOT NULL CONSTRAINT DF_matriculas_status DEFAULT 0,
        motivo_rejeicao VARCHAR(500) NULL,
        CONSTRAINT PK_matriculas PRIMARY KEY (nr),
        CONSTRAINT FK_matriculas_alunos FOREIGN KEY (aluno_codigo) REFERENCES dbo.alunos (codigo),
        CONSTRAINT FK_matriculas_turmas FOREIGN KEY (turma_codigo) REFERENCES dbo.turmas (codigo),
        CONSTRAINT CHK_matricula_status CHECK (status IN (0, 1, 2, 3, 4))
    );
GO --------------------------------------------------------------------------------
    -- 9. TABELA DE PLANEJAMENTO DE AULAS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.aulas_planejamentos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        professor_codigo INT NOT NULL,
        turma_codigo INT NOT NULL,
        data_aula DATE NOT NULL,
        atividade_dinamica VARCHAR(200) NOT NULL,
        descricao VARCHAR(MAX) NOT NULL,
        status INT NOT NULL CONSTRAINT DF_aulas_status DEFAULT 1,
        CONSTRAINT PK_aulas_planejamentos PRIMARY KEY (codigo),
        CONSTRAINT FK_aulas_planejamentos_professores FOREIGN KEY (professor_codigo) REFERENCES dbo.professores (pessoa_codigo),
        CONSTRAINT FK_aulas_planejamentos_turmas FOREIGN KEY (turma_codigo) REFERENCES dbo.turmas (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 10. TABELA DE DIÁRIO DE BORDO
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.diario_bordo (
        codigo INT IDENTITY(1, 1) NOT NULL,
        matricula_nr INT NOT NULL,
        data_registro DATE NOT NULL,
        compareceu BIT NOT NULL,
        alimentacao VARCHAR(50) NULL,
        sono VARCHAR(50) NULL,
        humor VARCHAR(50) NULL,
        banheiro_fralda VARCHAR(50) NULL,
        observacoes VARCHAR(500) NULL,
        CONSTRAINT PK_diario_bordo PRIMARY KEY (codigo),
        CONSTRAINT FK_diario_bordo_matriculas FOREIGN KEY (matricula_nr) REFERENCES dbo.matriculas (nr),
        CONSTRAINT CHK_logica_falta CHECK (
            (compareceu = 1)
            OR (
                compareceu = 0
                AND alimentacao IS NULL
                AND sono IS NULL
                AND humor IS NULL
                AND banheiro_fralda IS NULL
            )
        )
    );
GO --------------------------------------------------------------------------------
    -- 11. TABELA DE FREQUÊNCIA
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.frequencias (
        aula_planejamentos_codigo INT NOT NULL,
        matricula_codigo INT NOT NULL,
        status_presenca CHAR(1) NOT NULL,
        CONSTRAINT PK_frequencias PRIMARY KEY (aula_planejamentos_codigo, matricula_codigo),
        CONSTRAINT FK_frequencias_aulas_planejamentos FOREIGN KEY (aula_planejamentos_codigo) REFERENCES dbo.aulas_planejamentos (codigo),
        CONSTRAINT FK_frequencias_matriculas FOREIGN KEY (matricula_codigo) REFERENCES dbo.matriculas (nr)
    );
GO --------------------------------------------------------------------------------
    -- 12. TABELA DO SITE: COMUNICADOS E AVISOS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.comunicados_avisos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        titulo VARCHAR(150) NOT NULL,
        conteudo VARCHAR(MAX) NOT NULL,
        categoria VARCHAR(50) NOT NULL,
        data_publicacao DATETIME NOT NULL CONSTRAINT DF_comunicados_data DEFAULT GETDATE(),
        autor_codigo INT NOT NULL,
        status INT NOT NULL CONSTRAINT DF_comunicados_status DEFAULT 1,
        CONSTRAINT PK_comunicados_avisos PRIMARY KEY (codigo),
        CONSTRAINT FK_comunicados_pessoas FOREIGN KEY (autor_codigo) REFERENCES dbo.pessoas (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 13. TABELA DO SITE: GALERIA MULTIMÍDIA
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.galeria_fotos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        titulo VARCHAR(100) NOT NULL,
        descricao VARCHAR(250) NULL,
        caminho_imagem VARCHAR(300) NOT NULL,
        turma_codigo INT NULL,
        data_postagem DATETIME NOT NULL CONSTRAINT DF_galeria_data DEFAULT GETDATE(),
        autor_codigo INT NOT NULL,
        CONSTRAINT PK_galeria_fotos PRIMARY KEY (codigo),
        CONSTRAINT FK_galeria_turmas FOREIGN KEY (turma_codigo) REFERENCES dbo.turmas (codigo),
        CONSTRAINT FK_galeria_pessoas FOREIGN KEY (autor_codigo) REFERENCES dbo.pessoas (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 14. TABELA DO SITE: CANAL DE ATENDIMENTO
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.mensagens_contato (
        codigo INT IDENTITY(1, 1) NOT NULL,
        nome_remetente VARCHAR(100) NOT NULL,
        email_remetente VARCHAR(100) NOT NULL,
        telefone_remetente VARCHAR(15) NOT NULL,
        assunto VARCHAR(100) NOT NULL,
        mensagem VARCHAR(MAX) NOT NULL,
        data_envio DATETIME NOT NULL CONSTRAINT DF_contato_data DEFAULT GETDATE(),
        status_atendimento VARCHAR(20) NOT NULL CONSTRAINT DF_contato_status DEFAULT 'PENDENTE',
        CONSTRAINT PK_mensagens_contato PRIMARY KEY (codigo)
    );
GO
