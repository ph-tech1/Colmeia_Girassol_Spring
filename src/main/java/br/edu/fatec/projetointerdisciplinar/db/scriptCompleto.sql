-- =============================================================================
-- BANCO GIRASSOL - SCRIPT COMPLETO
-- =============================================================================
CREATE DATABASE banco_girassol;
GO

USE banco_girassol;
GO

--------------------------------------------------------------------------------
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
            'ADMIN'
        )
    )
);
GO

--------------------------------------------------------------------------------
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
GO

--------------------------------------------------------------------------------
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
GO

--------------------------------------------------------------------------------
-- 4. TABELA DE RESPONSAVEIS
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
GO

--------------------------------------------------------------------------------
-- 5. TABELA DE DOMINIO DE PARENTESCOS
--------------------------------------------------------------------------------
CREATE TABLE dbo.parentescos (
    codigo INT IDENTITY(1, 1) NOT NULL,
    descricao VARCHAR(50) NOT NULL,
    CONSTRAINT PK_parentescos PRIMARY KEY (codigo),
    CONSTRAINT UQ_parentescos_descricao UNIQUE (descricao)
);

INSERT INTO dbo.parentescos (descricao)
VALUES (N'Mãe'),
    (N'Pai'),
    (N'Avó'),
    (N'Avô'),
    (N'Tio'),
    (N'Tia'),
    (N'Irmão'),
    (N'Irmã'),
    (N'Padrasto'),
    (N'Madrasta'),
    (N'Responsável legal'),
    (N'Amigo(a) da família'),
    (N'Outro');
GO

--------------------------------------------------------------------------------
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
GO

--------------------------------------------------------------------------------
-- 7. RELACIONAMENTO RESPONSAVEL <-> ALUNO
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
GO

--------------------------------------------------------------------------------
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
GO

CREATE UNIQUE INDEX UX_autorizados_pessoa
    ON dbo.autorizados (pessoa_codigo)
    WHERE pessoa_codigo IS NOT NULL;
GO

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
GO

--------------------------------------------------------------------------------
-- 9. TABELA DE MATRICULAS
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
    CONSTRAINT CHK_matricula_status CHECK (status IN (0, 1, 2, 3, 4)) -- 0 pendente, 1 em analise, 2 aprovada, 3 rejeitada e 4 cancelada
);
GO

--------------------------------------------------------------------------------
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
GO

--------------------------------------------------------------------------------
-- 11. TABELA DE FREQUENCIA
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
GO

--------------------------------------------------------------------------------
-- 12. TABELA DE DIARIO DE BORDO (somente criancas presentes)
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
GO

--------------------------------------------------------------------------------
-- FUNCTIONS
--------------------------------------------------------------------------------
USE banco_girassol;
GO

-- Retorna o percentual de presenca atual de uma matricula.
-- O calculo usa todos os registros da tabela frequencias e considera
-- somente status 'P' como presenca; sem registros, retorna zero.
CREATE OR ALTER FUNCTION dbo.fn_percentual_presenca_matricula
(
    @matricula_codigo INT
)
RETURNS DECIMAL(5, 2)
AS
BEGIN
    DECLARE @percentual DECIMAL(5, 2);

    SELECT @percentual = CAST(
        COALESCE(
            100.0 * SUM(CASE WHEN status_presenca = 'P' THEN CONVERT(BIGINT, 1) ELSE CONVERT(BIGINT, 0) END)
                / NULLIF(COUNT_BIG(*), 0),
            0
        ) AS DECIMAL(5, 2)
    )
    FROM dbo.frequencias
    WHERE matricula_codigo = @matricula_codigo;

    RETURN @percentual;
END;
GO

-- Retorna o historico de frequencias registradas para uma matricula,
-- enriquecido com aula, turma, professor e dados do diario quando houver.
CREATE OR ALTER FUNCTION dbo.fn_historico_frequencia_matricula
(
    @matricula_codigo INT
)
RETURNS TABLE
AS
RETURN
(
    SELECT
        f.matricula_codigo,
        a.codigo AS aluno_codigo,
        a.nome AS aluno_nome,
        f.aula_planejamentos_codigo AS aula_codigo,
        ap.data_aula,
        ap.atividade_dinamica,
        ap.descricao AS aula_descricao,
        f.turma_codigo,
        t.nome_turma,
        professor.nome AS professor_nome,
        f.status_presenca,
        f.criado_em AS registrado_em,
        d.alimentacao,
        d.sono,
        d.humor,
        d.banheiro_fralda,
        d.observacoes
    FROM dbo.frequencias AS f
    JOIN dbo.matriculas AS m ON m.nr = f.matricula_codigo
    JOIN dbo.alunos AS a ON a.codigo = m.aluno_codigo
    JOIN dbo.aulas_planejamentos AS ap
        ON ap.codigo = f.aula_planejamentos_codigo
       AND ap.turma_codigo = f.turma_codigo
    JOIN dbo.turmas AS t ON t.codigo = f.turma_codigo
    JOIN dbo.pessoas AS professor ON professor.codigo = ap.professor_codigo
    LEFT JOIN dbo.diario_bordo AS d
        ON d.aula_planejamentos_codigo = f.aula_planejamentos_codigo
       AND d.matricula_codigo = f.matricula_codigo
    WHERE f.matricula_codigo = @matricula_codigo
);
GO

-- Funcao tabular de multiplas instrucoes que monta uma variavel de tabela
-- com os totais atuais da matricula. A linha so existe se a matricula existir.
CREATE OR ALTER FUNCTION dbo.fn_resumo_frequencia_matricula
(
    @matricula_codigo INT
)
RETURNS @resumo TABLE
(
    matricula_codigo INT NOT NULL,
    dias_registrados BIGINT NOT NULL,
    presencas BIGINT NOT NULL,
    faltas BIGINT NOT NULL,
    faltas_justificadas BIGINT NOT NULL
)
AS
BEGIN
    INSERT INTO @resumo (
        matricula_codigo,
        dias_registrados,
        presencas,
        faltas,
        faltas_justificadas
    )
    SELECT
        m.nr,
        COUNT_BIG(f.aula_planejamentos_codigo),
        COALESCE(SUM(CASE WHEN f.status_presenca = 'P' THEN CONVERT(BIGINT, 1) ELSE CONVERT(BIGINT, 0) END), 0),
        COALESCE(SUM(CASE WHEN f.status_presenca = 'F' THEN CONVERT(BIGINT, 1) ELSE CONVERT(BIGINT, 0) END), 0),
        COALESCE(SUM(CASE WHEN f.status_presenca = 'J' THEN CONVERT(BIGINT, 1) ELSE CONVERT(BIGINT, 0) END), 0)
    FROM dbo.matriculas AS m
    LEFT JOIN dbo.frequencias AS f ON f.matricula_codigo = m.nr
    WHERE m.nr = @matricula_codigo
    GROUP BY m.nr;

    RETURN;
END;
GO

--------------------------------------------------------------------------------
-- PROCEDURES
--------------------------------------------------------------------------------
USE banco_girassol;
GO

-- Avanca uma matricula pelas etapas administrativas permitidas:
-- pendente (0) para analise (1), e analise (1) para aprovada (2) ou
-- rejeitada (3). Rejeicao exige motivo; data e motivo sao atualizados
-- junto com o status na mesma transacao.
CREATE OR ALTER PROCEDURE dbo.sp_decidir_matricula
    @matricula_codigo INT,
    @novo_status INT,
    @motivo_rejeicao VARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @status_atual INT;

        SELECT @status_atual = status
        FROM dbo.matriculas WITH (UPDLOCK, HOLDLOCK)
        WHERE nr = @matricula_codigo;

        IF @status_atual IS NULL
            THROW 50001, N'Matrícula não encontrada.', 1;

        IF @novo_status IS NULL OR @novo_status NOT IN (1, 2, 3)
            THROW 50002, N'Status inválido para decisão de matrícula.', 1;

        IF NOT (
            (@status_atual = 0 AND @novo_status = 1)
            OR (@status_atual = 1 AND @novo_status IN (2, 3))
        )
            THROW 50002, N'Esta matrícula não pode ser alterada para o status informado.', 1;

        IF @novo_status = 3
            AND NULLIF(LTRIM(RTRIM(@motivo_rejeicao)), '') IS NULL
            THROW 50003, N'Informe o motivo da rejeição.', 1;

        UPDATE dbo.matriculas
        SET status = @novo_status,
            data_decisao = SYSDATETIME(),
            motivo_rejeicao = CASE WHEN @novo_status = 3 THEN @motivo_rejeicao ELSE NULL END
        WHERE nr = @matricula_codigo;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Atribui ou troca a turma de uma matricula aprovada.
-- O UPDATE condicional impede atribuir turma antes da aprovacao; a chave
-- estrangeira da tabela tambem garante que a turma informada exista.
CREATE OR ALTER PROCEDURE dbo.sp_atribuir_turma_matricula
    @matricula_codigo INT,
    @turma_codigo INT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM dbo.turmas WHERE codigo = @turma_codigo)
            THROW 50011, N'A turma selecionada não foi encontrada.', 1;

        UPDATE dbo.matriculas WITH (UPDLOCK, HOLDLOCK)
        SET turma_codigo = @turma_codigo
        WHERE nr = @matricula_codigo
          AND status = 2;

        IF @@ROWCOUNT = 0
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.matriculas WHERE nr = @matricula_codigo)
                THROW 50012, N'Matrícula não encontrada.', 1;
            THROW 50013, N'Só é possível atribuir turma a uma matrícula aprovada.', 1;
        END;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Registra a frequencia de um aluno para uma aula ou atualiza o registro
-- existente, validando a turma e o status de presenca. Nao permite marcar
-- falta ou justificativa enquanto houver diario de bordo para aquela aula.
CREATE OR ALTER PROCEDURE dbo.sp_registrar_frequencia
    @aula_codigo INT,
    @matricula_codigo INT,
    @turma_codigo INT,
    @status_presenca CHAR(1)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF @status_presenca IS NULL OR @status_presenca NOT IN ('P', 'F', 'J')
            THROW 50021, N'Informe uma presença válida (P, F ou J).', 1;

        IF NOT EXISTS (
            SELECT 1
            FROM dbo.aulas_planejamentos
            WHERE codigo = @aula_codigo
              AND turma_codigo = @turma_codigo
        )
            THROW 50022, N'A aula não pertence à turma informada.', 1;

        IF NOT EXISTS (
            SELECT 1
            FROM dbo.matriculas
            WHERE nr = @matricula_codigo
              AND turma_codigo = @turma_codigo
        )
            THROW 50023, N'A matrícula não pertence à turma informada.', 1;

        IF @status_presenca <> 'P'
           AND EXISTS (
               SELECT 1
               FROM dbo.diario_bordo
               WHERE aula_planejamentos_codigo = @aula_codigo
                 AND matricula_codigo = @matricula_codigo
           )
            THROW 50024, N'Apague o diário de bordo antes de alterar a presença para falta ou justificativa.', 1;

        -- HOLDLOCK evita que chamadas concorrentes insiram duas vezes a
        -- mesma combinacao de aula e matricula, que forma a chave primaria.
        MERGE dbo.frequencias WITH (HOLDLOCK) AS alvo
        USING (
            SELECT @aula_codigo AS aula_codigo,
                   @matricula_codigo AS matricula_codigo,
                   @turma_codigo AS turma_codigo,
                   @status_presenca AS status_presenca
        ) AS origem
        ON alvo.aula_planejamentos_codigo = origem.aula_codigo
           AND alvo.matricula_codigo = origem.matricula_codigo
        WHEN MATCHED THEN
            UPDATE SET turma_codigo = origem.turma_codigo,
                       status_presenca = origem.status_presenca
        WHEN NOT MATCHED THEN
            INSERT (aula_planejamentos_codigo, matricula_codigo, turma_codigo, status_presenca)
            VALUES (origem.aula_codigo, origem.matricula_codigo, origem.turma_codigo, origem.status_presenca);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

--------------------------------------------------------------------------------
-- TRIGGERS
--------------------------------------------------------------------------------
USE banco_girassol;
GO

-- Tabelas de auditoria sem chave estrangeira para preservar o historico
-- mesmo que o registro operacional deixe de existir.
IF OBJECT_ID(N'dbo.historico_status_matricula', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.historico_status_matricula (
        historico_codigo BIGINT IDENTITY(1, 1) NOT NULL
            CONSTRAINT PK_historico_status_matricula PRIMARY KEY,
        matricula_codigo INT NOT NULL,
        status_anterior INT NULL,
        status_novo INT NULL,
        operacao VARCHAR(10) NOT NULL,
        registrado_em DATETIME2 NOT NULL,
        usuario_banco SYSNAME NOT NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.historico_frequencias', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.historico_frequencias (
        historico_codigo BIGINT IDENTITY(1, 1) NOT NULL
            CONSTRAINT PK_historico_frequencias PRIMARY KEY,
        aula_planejamentos_codigo INT NOT NULL,
        matricula_codigo INT NOT NULL,
        turma_codigo INT NULL,
        turma_codigo_anterior INT NULL,
        status_anterior CHAR(1) NULL,
        status_novo CHAR(1) NULL,
        operacao VARCHAR(10) NOT NULL,
        registrado_em DATETIME2 NOT NULL,
        usuario_banco SYSNAME NOT NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.historico_diario_bordo', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.historico_diario_bordo (
        historico_codigo BIGINT IDENTITY(1, 1) NOT NULL
            CONSTRAINT PK_historico_diario_bordo PRIMARY KEY,
        aula_planejamentos_codigo INT NOT NULL,
        matricula_codigo INT NOT NULL,
        operacao VARCHAR(10) NOT NULL,
        campos_alterados NVARCHAR(300) NOT NULL,
        registrado_em DATETIME2 NOT NULL,
        usuario_banco SYSNAME NOT NULL
    );
END;
GO

-- Registra a inclusao e a exclusao da matricula, e cada alteracao real
-- de status. Updates que nao mudam o status nao geram historico.
CREATE OR ALTER TRIGGER dbo.tr_matriculas_historico_status
ON dbo.matriculas
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.historico_status_matricula (
        matricula_codigo,
        status_anterior,
        status_novo,
        operacao,
        registrado_em,
        usuario_banco
    )
    SELECT
        COALESCE(i.nr, d.nr),
        d.status,
        i.status,
        CASE
            WHEN d.nr IS NULL THEN 'INSERT'
            WHEN i.nr IS NULL THEN 'DELETE'
            ELSE 'UPDATE'
        END,
        SYSDATETIME(),
        SUSER_SNAME()
    FROM inserted AS i
    FULL OUTER JOIN deleted AS d ON d.nr = i.nr
    WHERE d.nr IS NULL
       OR i.nr IS NULL
       OR i.status <> d.status;
END;
GO

-- Mantem o historico de inclusoes, alteracoes e exclusoes de frequencia.
-- Em updates, somente status ou turma diferentes sao registrados.
CREATE OR ALTER TRIGGER dbo.tr_frequencias_historico
ON dbo.frequencias
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.historico_frequencias (
        aula_planejamentos_codigo,
        matricula_codigo,
        turma_codigo,
        turma_codigo_anterior,
        status_anterior,
        status_novo,
        operacao,
        registrado_em,
        usuario_banco
    )
    SELECT
        COALESCE(i.aula_planejamentos_codigo, d.aula_planejamentos_codigo),
        COALESCE(i.matricula_codigo, d.matricula_codigo),
        i.turma_codigo,
        d.turma_codigo,
        d.status_presenca,
        i.status_presenca,
        CASE
            WHEN d.aula_planejamentos_codigo IS NULL THEN 'INSERT'
            WHEN i.aula_planejamentos_codigo IS NULL THEN 'DELETE'
            ELSE 'UPDATE'
        END,
        SYSDATETIME(),
        SUSER_SNAME()
    FROM inserted AS i
    FULL OUTER JOIN deleted AS d
        ON d.aula_planejamentos_codigo = i.aula_planejamentos_codigo
       AND d.matricula_codigo = i.matricula_codigo
    WHERE d.aula_planejamentos_codigo IS NULL
       OR i.aula_planejamentos_codigo IS NULL
       OR ISNULL(i.turma_codigo, -1) <> ISNULL(d.turma_codigo, -1)
       OR ISNULL(i.status_presenca, '') <> ISNULL(d.status_presenca, '');
END;
GO

-- Audita as operacoes do diario sem copiar dados pessoais ou de rotina
-- para a tabela historica; em updates, registra somente os campos alterados.
CREATE OR ALTER TRIGGER dbo.tr_diario_bordo_historico
ON dbo.diario_bordo
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.historico_diario_bordo (
        aula_planejamentos_codigo,
        matricula_codigo,
        operacao,
        campos_alterados,
        registrado_em,
        usuario_banco
    )
    SELECT
        COALESCE(i.aula_planejamentos_codigo, d.aula_planejamentos_codigo),
        COALESCE(i.matricula_codigo, d.matricula_codigo),
        CASE
            WHEN d.aula_planejamentos_codigo IS NULL THEN 'INSERT'
            WHEN i.aula_planejamentos_codigo IS NULL THEN 'DELETE'
            ELSE 'UPDATE'
        END,
        CASE
            WHEN d.aula_planejamentos_codigo IS NULL THEN N'Registro criado'
            WHEN i.aula_planejamentos_codigo IS NULL THEN N'Registro removido'
            ELSE STUFF(CONCAT(
                CASE WHEN ISNULL(i.status_presenca, '') <> ISNULL(d.status_presenca, '')
                    THEN N', presença' ELSE N'' END,
                CASE WHEN ISNULL(i.registrado_por, -1) <> ISNULL(d.registrado_por, -1)
                    THEN N', professor responsável' ELSE N'' END,
                CASE WHEN ISNULL(i.alimentacao, '') <> ISNULL(d.alimentacao, '')
                    THEN N', alimentação' ELSE N'' END,
                CASE WHEN ISNULL(i.sono, '') <> ISNULL(d.sono, '')
                    THEN N', sono' ELSE N'' END,
                CASE WHEN ISNULL(i.humor, '') <> ISNULL(d.humor, '')
                    THEN N', humor' ELSE N'' END,
                CASE WHEN ISNULL(i.banheiro_fralda, '') <> ISNULL(d.banheiro_fralda, '')
                    THEN N', banheiro/fralda' ELSE N'' END,
                CASE WHEN ISNULL(i.observacoes, '') <> ISNULL(d.observacoes, '')
                    THEN N', observações' ELSE N'' END
            ), 1, 2, N'')
        END,
        SYSDATETIME(),
        SUSER_SNAME()
    FROM inserted AS i
    FULL OUTER JOIN deleted AS d
        ON d.aula_planejamentos_codigo = i.aula_planejamentos_codigo
       AND d.matricula_codigo = i.matricula_codigo
    WHERE d.aula_planejamentos_codigo IS NULL
       OR i.aula_planejamentos_codigo IS NULL
       OR ISNULL(i.status_presenca, '') <> ISNULL(d.status_presenca, '')
       OR ISNULL(i.registrado_por, -1) <> ISNULL(d.registrado_por, -1)
       OR ISNULL(i.alimentacao, '') <> ISNULL(d.alimentacao, '')
       OR ISNULL(i.sono, '') <> ISNULL(d.sono, '')
       OR ISNULL(i.humor, '') <> ISNULL(d.humor, '')
       OR ISNULL(i.banheiro_fralda, '') <> ISNULL(d.banheiro_fralda, '')
       OR ISNULL(i.observacoes, '') <> ISNULL(d.observacoes, '');
END;
GO

--------------------------------------------------------------------------------
-- VIEWS
--------------------------------------------------------------------------------
USE banco_girassol;
GO

-- Resume a frequencia de cada aluno em todas as matriculas:
-- contabiliza presencas, faltas e faltas justificadas e calcula o percentual
-- de presenca sobre os registros existentes. O LEFT JOIN mantem alunos sem
-- frequencia no resultado, com contadores e percentual iguais a zero.
CREATE OR ALTER VIEW dbo.vw_resumo_frequencia_aluno
AS
SELECT
    a.codigo AS aluno_codigo,
    a.nome AS aluno_nome,
    COUNT(f.aula_planejamentos_codigo) AS dias_registrados,
    SUM(CASE WHEN f.status_presenca = 'P' THEN 1 ELSE 0 END) AS presencas,
    SUM(CASE WHEN f.status_presenca = 'F' THEN 1 ELSE 0 END) AS faltas,
    SUM(CASE WHEN f.status_presenca = 'J' THEN 1 ELSE 0 END) AS faltas_justificadas,
    CAST(
        CASE
            WHEN COUNT(f.aula_planejamentos_codigo) = 0 THEN 0
            ELSE 100.0 * SUM(CASE WHEN f.status_presenca = 'P' THEN 1 ELSE 0 END)
                / COUNT(f.aula_planejamentos_codigo)
        END AS DECIMAL(5, 2)
    ) AS percentual_presenca
FROM dbo.alunos AS a
LEFT JOIN dbo.matriculas AS m ON m.aluno_codigo = a.codigo
LEFT JOIN dbo.frequencias AS f ON f.matricula_codigo = m.nr
GROUP BY a.codigo, a.nome;
GO

-- Lista alunos com matriculas aprovadas e turma atribuida, incluindo dados
-- da turma, professor e contato principal do responsavel. O OUTER APPLY
-- seleciona um unico responsavel por aluno, priorizando a ordem de contato.
CREATE OR ALTER VIEW dbo.vw_alunos_por_turma
AS
SELECT
    t.codigo AS turma_codigo,
    t.nome_turma,
    t.ano,
    t.grau,
    t.professor_codigo,
    pp.nome AS professor_nome,
    m.nr AS matricula_codigo,
    a.codigo AS aluno_codigo,
    a.nome AS aluno_nome,
    responsavel.nome AS responsavel_nome,
    responsavel.telefone AS responsavel_telefone
FROM dbo.matriculas AS m
JOIN dbo.alunos AS a ON a.codigo = m.aluno_codigo
JOIN dbo.turmas AS t ON t.codigo = m.turma_codigo
JOIN dbo.pessoas AS pp ON pp.codigo = t.professor_codigo
OUTER APPLY (
    SELECT TOP (1)
        p.nome,
        p.telefone
    FROM dbo.responsavel_aluno AS ra
    JOIN dbo.pessoas AS p ON p.codigo = ra.responsavel_codigo
    WHERE ra.aluno_codigo = a.codigo
    ORDER BY ra.ordem_contato, ra.responsavel_codigo
) AS responsavel
WHERE m.status = 2
  AND m.turma_codigo IS NOT NULL;
GO

-- Reune a frequencia e o diario de bordo com os dados da aula, turma,
-- professor, aluno e matricula. O LEFT JOIN preserva registros de frequencia
-- mesmo quando ainda nao existe diario (por exemplo, uma falta).
CREATE OR ALTER VIEW dbo.vw_diario_bordo_aluno
AS
SELECT
    a.codigo AS aluno_codigo,
    a.nome AS aluno_nome,
    m.nr AS matricula_codigo,
    t.codigo AS turma_codigo,
    t.nome_turma,
    ap.codigo AS aula_codigo,
    ap.data_aula,
    ap.atividade_dinamica,
    ap.descricao AS aula_descricao,
    ap.professor_codigo,
    professor.nome AS professor_nome,
    f.status_presenca,
    d.alimentacao,
    d.sono,
    d.humor,
    d.banheiro_fralda,
    d.observacoes
FROM dbo.frequencias AS f
JOIN dbo.matriculas AS m ON m.nr = f.matricula_codigo
JOIN dbo.alunos AS a ON a.codigo = m.aluno_codigo
JOIN dbo.aulas_planejamentos AS ap
    ON ap.codigo = f.aula_planejamentos_codigo
   AND ap.turma_codigo = f.turma_codigo
JOIN dbo.turmas AS t ON t.codigo = f.turma_codigo
JOIN dbo.pessoas AS professor ON professor.codigo = ap.professor_codigo
LEFT JOIN dbo.diario_bordo AS d
    ON d.aula_planejamentos_codigo = f.aula_planejamentos_codigo
   AND d.matricula_codigo = f.matricula_codigo;
GO