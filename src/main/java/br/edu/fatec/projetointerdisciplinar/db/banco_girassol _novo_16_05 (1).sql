-- Script de criacao do banco vazio com o esquema atual da aplicacao.
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
        sexo CHAR(1) NOT NULL,
        senha VARCHAR(255) NULL,
        tipo_perfil VARCHAR(20) NOT NULL CONSTRAINT DF_pessoas_perfil DEFAULT 'RESPONSAVEL',
        cep VARCHAR(8) NOT NULL,
        cidade VARCHAR(50) NOT NULL,
        uf CHAR(2) NOT NULL,
        bairro VARCHAR(60) NOT NULL,
        endereco VARCHAR(100) NOT NULL,
        numero VARCHAR(10) NOT NULL,
        complemento VARCHAR(50) NULL,
        telefone VARCHAR(15) NOT NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_pessoas_criado DEFAULT SYSDATETIME(),
        CONSTRAINT PK_pessoas PRIMARY KEY (codigo),
        CONSTRAINT UQ_pessoas_cpf UNIQUE (cpf),
        CONSTRAINT UQ_pessoas_email UNIQUE (email),
        CONSTRAINT CHK_pessoas_cpf CHECK (
            LEN(cpf) = 11
            AND cpf NOT LIKE '%[^0-9]%'
        ),
        CONSTRAINT CHK_pessoas_sexo CHECK (sexo IN ('F', 'M', 'O')),
        CONSTRAINT CHK_pessoas_perfil CHECK (
            tipo_perfil IN (
                'RESPONSAVEL',
                'PROFESSOR',
                'SECRETARIA',
                'ADMIN'
            )
        )
    );
GO --------------------------------------------------------------------------------
    -- 2. TABELA DE ALUNOS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.alunos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        data_nascimento DATE NOT NULL,
        sexo CHAR(1) NOT NULL,
        alergias VARCHAR(100) NULL,
        restricoes_alimentares VARCHAR(100) NULL,
        necessidades_especiais VARCHAR(MAX) NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_alunos_criado DEFAULT SYSDATETIME(),
        CONSTRAINT PK_alunos PRIMARY KEY (codigo),
        CONSTRAINT CHK_alunos_sexo CHECK (sexo IN ('F', 'M', 'O')),
        CONSTRAINT CHK_alunos_alergias CHECK (
            alergias IS NULL
            OR LEN(alergias) > 0
        ),
        CONSTRAINT CHK_alunos_restricoes CHECK (
            restricoes_alimentares IS NULL
            OR LEN(restricoes_alimentares) > 0
        )
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
        local_trabalho VARCHAR(100) NULL,
        telefone_trabalho VARCHAR(15) NULL,
        estado_civil VARCHAR(20) NULL,
        status INT NOT NULL CONSTRAINT DF_responsaveis_status DEFAULT 1,
        CONSTRAINT PK_responsaveis PRIMARY KEY (pessoa_codigo),
        CONSTRAINT FK_responsaveis_pessoas FOREIGN KEY (pessoa_codigo) REFERENCES dbo.pessoas (codigo),
        CONSTRAINT CHK_responsaveis_estado_civil CHECK (
            estado_civil IN (
                'SOLTEIRO',
                'CASADO',
                'UNIAO_ESTAVEL',
                'SEPARADO',
                'DIVORCIADO',
                'VIUVO'
            )
        )
    );
GO --------------------------------------------------------------------------------
    -- 5. TABELA DE DOMÍNIO DE PARENTESCOS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.parentescos (
        codigo INT IDENTITY(1, 1) NOT NULL,
        descricao VARCHAR(50) NOT NULL,
        CONSTRAINT PK_parentescos PRIMARY KEY (codigo),
        CONSTRAINT UQ_parentescos_descricao UNIQUE (descricao)
    );
INSERT INTO dbo.parentescos (descricao)
VALUES ('Mãe'),
    ('Pai'),
    ('Avó'),
    ('Avô'),
    ('Tio'),
    ('Tia'),
    ('Irmão'),
    ('Irmã'),
    ('Padrasto'),
    ('Madrasta'),
    ('Responsável legal'),
    ('Amigo(a) da família'),
    ('Outro');
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
        parentesco_codigo INT NOT NULL,
        esp_financeiro BIT NOT NULL,
        ordem_contato INT NOT NULL,
        CONSTRAINT PK_responsavel_aluno PRIMARY KEY (responsavel_codigo, aluno_codigo),
        CONSTRAINT FK_responsavel_aluno_responsaveis FOREIGN KEY (responsavel_codigo) REFERENCES dbo.responsaveis (pessoa_codigo),
        CONSTRAINT FK_responsavel_aluno_alunos FOREIGN KEY (aluno_codigo) REFERENCES dbo.alunos (codigo),
        CONSTRAINT FK_responsavel_aluno_parentescos FOREIGN KEY (parentesco_codigo) REFERENCES dbo.parentescos (codigo)
    );
GO --------------------------------------------------------------------------------
    -- 8. PESSOAS AUTORIZADAS A BUSCAR
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.autorizados (
        codigo INT IDENTITY(1, 1) NOT NULL,
        nome VARCHAR(100) NULL,
        cpf VARCHAR(11) NOT NULL,
        rg VARCHAR(20) NULL,
        telefone VARCHAR(15) NULL,
        pessoa_codigo INT NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_autorizados_criado DEFAULT SYSDATETIME(),
        CONSTRAINT PK_autorizados PRIMARY KEY (codigo),
        CONSTRAINT UQ_autorizados_cpf UNIQUE (cpf),
        CONSTRAINT FK_autorizados_pessoas FOREIGN KEY (pessoa_codigo) REFERENCES dbo.pessoas (codigo),
        CONSTRAINT CHK_autorizados_cpf CHECK (
            LEN(cpf) = 11
            AND cpf NOT LIKE '%[^0-9]%'
        )
    );
CREATE UNIQUE INDEX UX_autorizados_pessoa
    ON dbo.autorizados (pessoa_codigo)
    WHERE pessoa_codigo IS NOT NULL;
CREATE TABLE dbo.autorizados_busca (
    codigo INT IDENTITY(1, 1) NOT NULL,
    autorizado_codigo INT NOT NULL,
    aluno_codigo INT NOT NULL,
    responsavel_codigo INT NOT NULL,
    parentesco_codigo INT NOT NULL,
    ativo BIT NOT NULL CONSTRAINT DF_ab_ativo DEFAULT 1,
    criado_em DATETIME2 NOT NULL CONSTRAINT DF_ab_criado DEFAULT SYSDATETIME(),
    CONSTRAINT PK_autorizados_busca PRIMARY KEY (codigo),
    CONSTRAINT UQ_ab_autorizado_aluno UNIQUE (autorizado_codigo, aluno_codigo),
    CONSTRAINT FK_ab_autorizados FOREIGN KEY (autorizado_codigo) REFERENCES dbo.autorizados (codigo),
    CONSTRAINT FK_ab_parentescos FOREIGN KEY (parentesco_codigo) REFERENCES dbo.parentescos (codigo),
    CONSTRAINT FK_ab_responsavel_aluno FOREIGN KEY (responsavel_codigo, aluno_codigo) REFERENCES dbo.responsavel_aluno (responsavel_codigo, aluno_codigo)
);
GO --------------------------------------------------------------------------------
    -- 9. TABELA DE MATRÍCULAS
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.matriculas (
        nr INT IDENTITY(1, 1) NOT NULL,
        aluno_codigo INT NOT NULL,
        turma_codigo INT NULL,
        data_envio DATETIME NOT NULL CONSTRAINT DF_matriculas_envio DEFAULT GETDATE(),
        data_decisao DATETIME NULL,
        status INT NOT NULL CONSTRAINT DF_matriculas_status DEFAULT 0,
        motivo_rejeicao VARCHAR(500) NULL,
        CONSTRAINT PK_matriculas PRIMARY KEY (nr),
        CONSTRAINT FK_matriculas_alunos FOREIGN KEY (aluno_codigo) REFERENCES dbo.alunos (codigo),
        CONSTRAINT FK_matriculas_turmas FOREIGN KEY (turma_codigo) REFERENCES dbo.turmas (codigo),
        CONSTRAINT UQ_matriculas_nr_turma UNIQUE (nr, turma_codigo),
        CONSTRAINT CHK_matricula_status CHECK (status IN (0, 1, 2, 3, 4)) -- 0 pedente, 1 em analize, 2 aprovado, 3 rejeitado e 4 cancelada
    );
GO --------------------------------------------------------------------------------
    -- 10. TABELA DE PLANEJAMENTO DE AULAS
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
        CONSTRAINT FK_aulas_planejamentos_turmas FOREIGN KEY (turma_codigo) REFERENCES dbo.turmas (codigo),
        CONSTRAINT UQ_aulas_codigo_turma UNIQUE (codigo, turma_codigo),
        CONSTRAINT UQ_aulas_turma_data UNIQUE (turma_codigo, data_aula)
    );
GO --------------------------------------------------------------------------------
    -- 11. TABELA DE FREQUÊNCIA
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.frequencias (
        aula_planejamentos_codigo INT NOT NULL,
        matricula_codigo INT NOT NULL,
        turma_codigo INT NOT NULL,
        status_presenca CHAR(1) NOT NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_freq_criado DEFAULT SYSDATETIME(),
        CONSTRAINT PK_frequencias PRIMARY KEY (aula_planejamentos_codigo, matricula_codigo),
        CONSTRAINT UQ_frequencias_status UNIQUE (
            aula_planejamentos_codigo,
            matricula_codigo,
            status_presenca
        ),
        CONSTRAINT CHK_frequencias_status CHECK (status_presenca IN ('P', 'F', 'J')),
        CONSTRAINT FK_frequencias_aula_turma FOREIGN KEY (aula_planejamentos_codigo, turma_codigo) REFERENCES dbo.aulas_planejamentos (codigo, turma_codigo),
        CONSTRAINT FK_frequencias_matricula_turma FOREIGN KEY (matricula_codigo, turma_codigo) REFERENCES dbo.matriculas (nr, turma_codigo)
    );
GO --------------------------------------------------------------------------------
    -- 12. TABELA DE DIÁRIO DE BORDO (somente crianças presentes)
    --------------------------------------------------------------------------------
    CREATE TABLE dbo.diario_bordo (
        aula_planejamentos_codigo INT NOT NULL,
        matricula_codigo INT NOT NULL,
        status_presenca CHAR(1) NOT NULL CONSTRAINT DF_diario_presenca DEFAULT 'P',
        registrado_por INT NOT NULL,
        alimentacao VARCHAR(50) NULL,
        sono VARCHAR(50) NULL,
        humor VARCHAR(50) NULL,
        banheiro_fralda VARCHAR(50) NULL,
        observacoes VARCHAR(500) NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_diario_criado DEFAULT SYSDATETIME(),
        CONSTRAINT PK_diario_bordo PRIMARY KEY (aula_planejamentos_codigo, matricula_codigo),
        CONSTRAINT CHK_diario_so_presente CHECK (status_presenca = 'P'),
        CONSTRAINT FK_diario_frequencia FOREIGN KEY (
            aula_planejamentos_codigo,
            matricula_codigo,
            status_presenca
        ) REFERENCES dbo.frequencias (
            aula_planejamentos_codigo,
            matricula_codigo,
            status_presenca
        ),
        CONSTRAINT FK_diario_professor FOREIGN KEY (registrado_por) REFERENCES dbo.professores (pessoa_codigo)
    );
GO --------------------------------------------------------------------------------
    -- 13. TABELA DO SITE: COMUNICADOS E AVISOS
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
    -- 14. TABELA DO SITE: GALERIA MULTIMÍDIA
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
    -- 15. TABELA DO SITE: CANAL DE ATENDIMENTO
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
