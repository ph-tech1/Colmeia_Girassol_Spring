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
