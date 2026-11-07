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
