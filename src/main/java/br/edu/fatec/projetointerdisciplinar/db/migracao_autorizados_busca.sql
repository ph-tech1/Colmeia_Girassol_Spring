USE banco_girassol;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    ALTER TABLE dbo.autorizados_busca
        DROP CONSTRAINT FK_autorizados_busca_pessoas;
    ALTER TABLE dbo.autorizados_busca
        DROP CONSTRAINT PK_autorizados_busca;

    ALTER TABLE dbo.autorizados_busca
        ADD nome VARCHAR(100) NULL,
            telefone VARCHAR(15) NULL;

    UPDATE autorizado
    SET nome = pessoa.nome,
        telefone = pessoa.telefone
    FROM dbo.autorizados_busca autorizado
    INNER JOIN dbo.pessoas pessoa ON pessoa.codigo = autorizado.codigo;

    ALTER TABLE dbo.autorizados_busca
        ALTER COLUMN nome VARCHAR(100) NOT NULL;
    ALTER TABLE dbo.autorizados_busca
        ALTER COLUMN telefone VARCHAR(15) NOT NULL;

    ALTER TABLE dbo.autorizados_busca
        ADD novo_codigo INT IDENTITY(1, 1) NOT NULL;
    ALTER TABLE dbo.autorizados_busca
        DROP COLUMN codigo;
    EXEC sp_rename 'dbo.autorizados_busca.novo_codigo', 'codigo', 'COLUMN';

    ALTER TABLE dbo.autorizados_busca
        ADD CONSTRAINT PK_autorizados_busca PRIMARY KEY (codigo);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
