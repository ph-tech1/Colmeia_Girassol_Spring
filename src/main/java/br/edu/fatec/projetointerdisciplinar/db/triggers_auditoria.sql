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
