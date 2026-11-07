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
