-- =============================================================================
-- Inserts
-- =============================================================================
USE banco_girassol;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    ---------------------------------------------------------------------------
    -- 1. PESSOAS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.pessoas
        (nome, email, cpf, rg, data_nascimento, sexo, senha, tipo_perfil,
         cep, cidade, uf, bairro, endereco, numero, complemento, telefone)
    VALUES
    -- Professores
    ('Ana Paula Ferreira',  'ana.ferreira@girassol.com',  '11111111101', '12.345.678-1', '1988-04-12', 'F', '$2a$10$3HwylUjOEl/p557H6r0P/eS/c0cECAhLPsCLpeISMV0XbMGXEIp/e', 'PROFESSOR',
     '15025000', 'Sao Jose do Rio Preto', 'SP', 'Centro',        'Rua Bernardino de Campos', '450', NULL,      '(17) 99111-0001'),
    ('Bruno Henrique Lima', 'bruno.lima@girassol.com',    '11111111102', '23.456.789-2', '1990-09-23', 'M', '$2a$10$3HwylUjOEl/p557H6r0P/eS/c0cECAhLPsCLpeISMV0XbMGXEIp/e', 'PROFESSOR',
     '15030100', 'Sao Jose do Rio Preto', 'SP', 'Boa Vista',     'Av. Alberto Andalo',       '1200', 'Apto 31', '(17) 99111-0002'),
    ('Camila Rodrigues',    'camila.rodrigues@girassol.com','11111111103','34.567.890-3','1985-01-30', 'F', '$2a$10$3HwylUjOEl/p557H6r0P/eS/c0cECAhLPsCLpeISMV0XbMGXEIp/e', 'PROFESSOR',
     '15040200', 'Sao Jose do Rio Preto', 'SP', 'Vila Imperial', 'Rua Voluntarios de Sao Paulo','88', NULL,    '(17) 99111-0003'),
    -- Responsaveis
    ('Mariana Souza Alves', 'mariana.alves@email.com',    '22222222201', '45.678.901-4', '1992-06-15', 'F', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15050300', 'Sao Jose do Rio Preto', 'SP', 'Jardim Nazareth','Rua das Palmeiras',       '120', NULL,      '(17) 99222-0001'),
    ('Carlos Eduardo Pinto','carlos.pinto@email.com',     '22222222202', '56.789.012-5', '1989-11-02', 'M', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15060400', 'Sao Jose do Rio Preto', 'SP', 'Vila Elvira',   'Rua Sete de Setembro',     '305', 'Casa 2',  '(17) 99222-0002'),
    ('Fernanda Gomes Costa','fernanda.costa@email.com',   '22222222203', '67.890.123-6', '1994-02-20', 'F', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15070500', 'Sao Jose do Rio Preto', 'SP', 'Redentora',     'Av. Brasil',               '2500', 'Bloco B', '(17) 99222-0003'),
    ('Roberto Nunes Dias',  'roberto.dias@email.com',     '22222222204', '78.901.234-7', '1987-08-09', 'M', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15080600', 'Sao Jose do Rio Preto', 'SP', 'Higienopolis',  'Rua Prudente de Moraes',   '77', NULL,       '(17) 99222-0004'),
    ('Helena Martins Rocha','helena.rocha@email.com',     '22222222205', '89.012.345-8', '1965-12-25', 'F', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15090700', 'Sao Jose do Rio Preto', 'SP', 'Santa Cruz',    'Rua Gabriel Monteiro',     '640', NULL,      '(17) 99222-0005');

    ---------------------------------------------------------------------------
    -- 2. PROFESSORES
    ---------------------------------------------------------------------------
    INSERT INTO dbo.professores (pessoa_codigo, data_contratacao, formacao, status)
    VALUES
    (1, '2020-02-03', 'Pedagogia',                     1),
    (2, '2021-08-16', 'Pedagogia com Especializacao em Educacao Infantil', 1),
    (3, '2019-01-21', 'Normal Superior',               1);

    ---------------------------------------------------------------------------
    -- 3. RESPONSAVEIS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.responsaveis (pessoa_codigo, local_trabalho, telefone_trabalho, estado_civil, status)
    VALUES
    (4, 'Clinica Odontologica Sorriso', '(17) 3222-1001', 'CASADO',        1),
    (5, 'Construtora Alvorada',         '(17) 3222-1002', 'CASADO',        1),
    (6, 'Hospital Santa Casa',          '(17) 3222-1003', 'UNIAO_ESTAVEL', 1),
    (7, 'Banco Central Regional',       '(17) 3222-1004', 'DIVORCIADO',    1),
    (8, NULL,                           NULL,             'VIUVO',         1);

    ---------------------------------------------------------------------------
    -- 4. ALUNOS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.alunos
        (nome, data_nascimento, sexo, alergias, restricoes_alimentares, necessidades_especiais)
    VALUES
    ('Lucas Alves Souza',    '2025-03-10', 'M', NULL,              NULL,                  NULL),               -- 1 (resp. 4)
    ('Sofia Alves Souza',    '2022-05-18', 'F', 'Amendoim',        NULL,                  NULL),               -- 2 (resp. 4)
    ('Miguel Pinto Neto',    '2025-06-22', 'M', NULL,              'Sem lactose',         NULL),               -- 3 (resp. 5)
    ('Helena Pinto Neto',    '2023-08-05', 'F', NULL,              NULL,                  NULL),               -- 4 (resp. 5)
    ('Arthur Costa Lima',    '2023-02-14', 'M', 'Picada de inseto',NULL,                  NULL),               -- 5 (resp. 6)
    ('Laura Costa Lima',     '2021-11-30', 'F', NULL,              NULL,                  'Acompanhamento fonoaudiologico semanal'), -- 6 (resp. 6)
    ('Davi Dias Nunes',      '2022-09-09', 'M', NULL,              'Vegetariano',         NULL),               -- 7 (resp. 7)
    ('Alice Dias Nunes',     '2025-01-27', 'F', NULL,              NULL,                  NULL),               -- 8 (resp. 7)
    ('Heitor Rocha Martins', '2023-04-03', 'M', 'Corante artificial', NULL,               NULL),               -- 9 (resp. 8)
    ('Valentina Rocha Martins','2024-10-12','F', NULL,             NULL,                  NULL);               -- 10 (resp. 8)

    ---------------------------------------------------------------------------
    -- 5. TURMAS (parentescos ja sao inseridos no script de criacao)
    ---------------------------------------------------------------------------
    INSERT INTO dbo.turmas (professor_codigo, nome_turma, ano, grau)
    VALUES
    (1, 'Bercario Sol',        2026, 'Bercario'),    -- turma 1 - Ana Paula
    (2, 'Maternal Estrelinha', 2026, 'Maternal'),    -- turma 2 - Bruno Henrique
    (3, 'Jardim Arco-Iris',    2026, 'Pre-escola');  -- turma 3 - Camila

    ---------------------------------------------------------------------------
    -- 6. RESPONSAVEL x ALUNO
    -- parentesco_codigo: 1 = Mae | 2 = Pai | 3 = Avo
    ---------------------------------------------------------------------------
    INSERT INTO dbo.responsavel_aluno
        (responsavel_codigo, aluno_codigo, parentesco_codigo, esp_financeiro, ordem_contato)
    VALUES
    (4, 1, 1, 1, 1), (4, 2, 1, 1, 1),    -- Mariana  -> Lucas, Sofia   (Mae)
    (5, 3, 2, 1, 1), (5, 4, 2, 1, 1),    -- Carlos   -> Miguel, Helena (Pai)
    (6, 5, 1, 1, 1), (6, 6, 1, 1, 1),    -- Fernanda -> Arthur, Laura  (Mae)
    (7, 7, 2, 1, 1), (7, 8, 2, 1, 1),    -- Roberto  -> Davi, Alice    (Pai)
    (8, 9, 3, 1, 1), (8, 10, 3, 1, 1);   -- Helena   -> Heitor, Valentina (Avo)

    ---------------------------------------------------------------------------
    -- 7. NOVOS RESPONSAVEIS E ALUNOS PARA DIVERSIFICAR DASHBOARD
    ---------------------------------------------------------------------------
    INSERT INTO dbo.pessoas
        (nome, email, cpf, rg, data_nascimento, sexo, senha, tipo_perfil,
         cep, cidade, uf, bairro, endereco, numero, complemento, telefone)
    VALUES
    ('Patricia Lima Rocha', 'patricia.rocha@email.com', '22222222206', '90.123.456-9', '1988-03-16', 'F', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15020800', 'Sao Jose do Rio Preto', 'SP', 'Jardim Paulista', 'Rua das Acacias', '300', 'Casa', '(17) 99222-0006'),
    ('Jonas Teixeira', 'jonas.teixeira@email.com', '22222222207', '01.234.567-0', '1981-07-28', 'M', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15010900', 'Sao Jose do Rio Preto', 'SP', 'Jardim Pq. das Flores', 'Av. das Nações', '415', NULL, '(17) 99222-0007'),
    ('Andreia Campos', 'andreia.campos@email.com', '22222222208', '12.345.678-9', '1991-01-10', 'F', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15030400', 'Sao Jose do Rio Preto', 'SP', 'Pq. dos Ipês', 'Rua Itapetinga', '76', 'Apto 32', '(17) 99222-0008'),
    ('Renato Pereira', 'renato.pereira@email.com', '22222222209', '23.456.789-0', '1986-09-14', 'M', '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG', 'RESPONSAVEL',
     '15040600', 'Sao Jose do Rio Preto', 'SP', 'Vila Operaria', 'Rua Campos Sales', '920', NULL, '(17) 99222-0009');

    INSERT INTO dbo.responsaveis (pessoa_codigo, local_trabalho, telefone_trabalho, estado_civil, status)
    VALUES
    (9, 'Laboratorio Analitico', '(17) 3344-1001', 'SOLTEIRO', 1),
    (10, 'Loja de Móveis Casa Nova', '(17) 3344-1002', 'CASADO', 1),
    (11, 'Consultorio de Fisioterapia', '(17) 3344-1003', 'DIVORCIADO', 1),
    (12, 'Escola de Idiomas Viva', '(17) 3344-1004', 'CASADO', 1);

    INSERT INTO dbo.alunos
        (nome, data_nascimento, sexo, alergias, restricoes_alimentares, necessidades_especiais)
    VALUES
    ('Theo Lima Rocha',      '2023-07-12', 'M', NULL, NULL, NULL),
    ('Nina Teixeira Almeida','2022-10-05', 'F', NULL, NULL, NULL),
    ('Gael Campos Silva',    '2024-01-18', 'M', 'Leite', NULL, NULL),
    ('Livia Pereira Costa',  '2021-06-03', 'F', NULL, NULL, 'Acompanhamento psicomotor'),
    ('Joao Pedro Nascimento','2025-02-11', 'M', NULL, NULL, NULL),
    ('Isabela Moreira',     '2023-09-14', 'F', NULL, NULL, NULL);

    INSERT INTO dbo.responsavel_aluno
        (responsavel_codigo, aluno_codigo, parentesco_codigo, esp_financeiro, ordem_contato)
    VALUES
    (9, 11, 1, 1, 1),
    (9, 12, 1, 1, 1),
    (10, 13, 2, 1, 1),
    (11, 14, 1, 1, 1),
    (12, 15, 2, 1, 1),
    (12, 16, 2, 1, 1);

    ---------------------------------------------------------------------------
    -- 8. MATRICULAS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.matriculas
        (aluno_codigo, turma_codigo, data_envio, data_decisao, status, motivo_rejeicao)
    VALUES
    (1,  1,    '2026-01-12T09:00:00', '2026-01-20T14:00:00', 2, NULL),  -- Lucas     -> Bercario
    (2,  3,    '2026-01-12T09:05:00', '2026-01-20T14:10:00', 2, NULL),  -- Sofia     -> Jardim
    (3,  1,    '2026-01-13T10:00:00', '2026-01-21T11:00:00', 2, NULL),  -- Miguel    -> Bercario
    (4,  2,    '2026-01-13T10:05:00', '2026-01-21T11:10:00', 2, NULL),  -- Helena    -> Maternal
    (5,  2,    '2026-01-14T08:30:00', '2026-01-22T15:00:00', 2, NULL),  -- Arthur    -> Maternal
    (6,  3,    '2026-01-14T08:35:00', '2026-01-22T15:05:00', 2, NULL),  -- Laura     -> Jardim
    (7,  3,    '2026-01-15T13:00:00', '2026-01-23T10:00:00', 2, NULL),  -- Davi      -> Jardim
    (8,  1,    '2026-01-15T13:05:00', '2026-01-23T10:05:00', 2, NULL),  -- Alice     -> Bercario
    (9,  2,    '2026-01-16T16:00:00', '2026-01-26T09:00:00', 2, NULL),  -- Heitor    -> Maternal
    (10, NULL, '2026-02-02T11:00:00', NULL,                  1, NULL),  -- Valentina -> em analise (sem turma ainda)
    (11, NULL, '2026-02-10T09:30:00', NULL,                  0, NULL),  -- Theo -> pendente, sem responsavel definido ainda
    (12, NULL, '2026-02-11T10:15:00', NULL,                  0, NULL),  -- Nina -> pendente
    (13, 1,    '2026-02-11T14:00:00', '2026-02-13T16:30:00', 3, 'Documentação incompleta do responsável'),
    (14, NULL, '2026-02-12T08:45:00', NULL,                  1, NULL),  -- Livia -> em analise
    (15, NULL, '2026-02-13T11:00:00', NULL,                  0, NULL),  -- Joao -> pendente, sem solicitacao do responsavel
    (16, 1,    '2026-02-14T09:00:00', '2026-02-16T12:00:00', 3, 'Faixa etaria divergente da turma escolhida');

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Conferencia rapida
SELECT * FROM dbo.vw_alunos_por_turma ORDER BY turma_codigo, aluno_nome;
GO

-- =============================================================================
-- =============================================================================

BEGIN TRY
    BEGIN TRANSACTION;

    ---------------------------------------------------------------------------
    -- 1. AULAS PLANEJADAS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.aulas_planejamentos
        (professor_codigo, turma_codigo, data_aula, atividade_dinamica, descricao)
    VALUES
    -- Ana Paula - Bercario Sol
    (1, 1, '2026-11-12', N'Estimulação sensorial com texturas',
        N'Exploração de tecidos, esponjas e folhas secas em tapete sensorial, com acompanhamento individual de cada bebê.'),
    (1, 1, '2026-11-13', N'Música e movimento com chocalhos',
        N'Canções de roda e sons de chocalhos para estimular a audição e a coordenação motora.'),
    (1, 1, '2026-11-16', N'Brincadeira com bolhas de sabão',
        N'Os bebês acompanham e tentam tocar as bolhas, trabalhando atenção visual e movimento de braços.'),
    (1, 1, '2026-11-17', N'Tapete de atividades e engatinhar',
        N'Circuito baixo com almofadas e brinquedos de apoio para incentivar o engatinhar e o equilíbrio.'),
    (1, 1, '2026-11-18', N'Contação de história com livros de pano',
        N'Leitura mediada com livros de pano coloridos, com toque e identificação de figuras.'),

    -- Bruno Henrique - Maternal Estrelinha
    (2, 2, '2026-11-12', N'Pintura com os dedos',
        N'Pintura livre com tinta atóxica em papel craft, explorando cores e sensações.'),
    (2, 2, '2026-11-13', N'Massinha de modelar',
        N'Modelagem de bolinhas, cobrinhas e formas simples para desenvolver a coordenação motora fina.'),
    (2, 2, '2026-11-16', N'Circuito motor com almofadas',
        N'Percurso com obstáculos macios: pular, rastejar e equilibrar-se, trabalhando coordenação ampla.'),
    (2, 2, '2026-11-17', N'Roda de música e cantigas',
        N'Cantigas de roda com gestos e instrumentos simples, estimulando ritmo e linguagem.'),
    (2, 2, '2026-11-18', N'Faz de conta: mercadinho',
        N'Brincadeira simbólica de comprar e vender com frutas de brinquedo, incentivando a socialização.'),

    -- Camila - Jardim Arco-Iris
    (3, 3, '2026-11-12', N'Roda de conversa e alfabeto móvel',
        N'Conversa sobre o fim de semana e montagem dos nomes das crianças com letras móveis.'),
    (3, 3, '2026-11-13', N'Experiência com plantas',
        N'Plantio de feijão no algodão e registro por desenho do que as crianças esperam observar.'),
    (3, 3, '2026-11-16', N'Jogo de formas e cores',
        N'Classificação de peças por forma e cor em jogo coletivo, trabalhando raciocínio lógico.'),
    (3, 3, '2026-11-17', N'Teatro de fantoches',
        N'Apresentação de uma história com fantoches e recontagem pelas próprias crianças.'),
    (3, 3, '2026-11-18', N'Desenho livre e colagem',
        N'Produção de painel coletivo com desenho, recorte e colagem de materiais recicláveis.');

    ---------------------------------------------------------------------------
    -- 2. FREQUENCIAS DAS AULAS PLANEJADAS
    ---------------------------------------------------------------------------
    INSERT INTO dbo.frequencias
        (aula_planejamentos_codigo, matricula_codigo, turma_codigo, status_presenca)
    VALUES
    -- Bercario (aula 1)
    (1, 1, 1, 'P'),
    (1, 3, 1, 'P'),
    (1, 8, 1, 'F'),
    -- Maternal (aula 6)
    (6, 4, 2, 'P'),
    (6, 5, 2, 'J'),
    (6, 9, 2, 'P'),
    -- Jardim (aula 11)
    (11, 2, 3, 'P'),
    (11, 6, 3, 'P'),
    (11, 7, 3, 'P'),
    -- 13/11 - aulas 2, 7 e 12
    (2, 1, 1, 'P'),
    (2, 3, 1, 'P'),
    (2, 8, 1, 'P'),
    (7, 4, 2, 'P'),
    (7, 5, 2, 'P'),
    (7, 9, 2, 'F'),
    (12, 2, 3, 'P'),
    (12, 6, 3, 'P'),
    (12, 7, 3, 'P'),
    -- 16/11 - aulas 3, 8 e 13
    (3, 1, 1, 'P'),
    (3, 3, 1, 'J'),
    (3, 8, 1, 'P'),
    (8, 4, 2, 'P'),
    (8, 5, 2, 'P'),
    (8, 9, 2, 'P'),
    (13, 2, 3, 'F'),
    (13, 6, 3, 'P'),
    (13, 7, 3, 'P'),
    -- 17/11 - aulas 4, 9 e 14
    (4, 1, 1, 'P'),
    (4, 3, 1, 'P'),
    (4, 8, 1, 'F'),
    (9, 4, 2, 'F'),
    (9, 5, 2, 'P'),
    (9, 9, 2, 'P'),
    (14, 2, 3, 'P'),
    (14, 6, 3, 'P'),
    (14, 7, 3, 'P'),
    -- 18/11 - aulas 5, 10 e 15
    (5, 1, 1, 'P'),
    (5, 3, 1, 'P'),
    (5, 8, 1, 'P'),
    (10, 4, 2, 'P'),
    (10, 5, 2, 'J'),
    (10, 9, 2, 'P'),
    (15, 2, 3, 'P'),
    (15, 6, 3, 'F'),
    (15, 7, 3, 'P');

    ---------------------------------------------------------------------------
    -- 3. DIARIO DE BORDO (somente criancas presentes) - registrado_por = professor
    ---------------------------------------------------------------------------
    INSERT INTO dbo.diario_bordo
        (aula_planejamentos_codigo, matricula_codigo, registrado_por,
         alimentacao, sono, humor, banheiro_fralda, observacoes)
    VALUES
    -- Ana Paula (professor 1)
    (1, 1, 1, N'Comeu bem', N'Dormiu 1h30',   N'Tranquilo',  N'3 fraldas trocadas', N'Explorou bastante o tapete sensorial.'),
    (1, 3, 1, N'Comeu bem', N'Dormiu 1h',     N'Feliz', N'2 fraldas trocadas', N'Reagiu bem à música.'),
    -- Bruno Henrique (professor 2)
    (6, 4, 2, N'Comeu pouco', N'Dormiu 1h',     N'Feliz',    N'Foi ao banheiro 2x', N'Gostou muito da pintura com os dedos.'),
    (6, 9, 2, N'Comeu bem',  N'Dormiu 1h20',   N'Agitado',    N'Foi ao banheiro 3x', N'Atenção ao corante artificial no lanche.'),
    -- Camila (professor 3)
    (11, 2, 3, N'Comeu tudo',            N'Não dormiu',    N'Feliz',    N'Foi ao banheiro 2x', N'Participou da roda de conversa.'),
    (11, 6, 3, N'Comeu bem',  N'Dormiu 30 min', N'Tranquilo',  N'Foi ao banheiro 1x', N'Montou o próprio nome com as letras.'),
    (11, 7, 3, N'Comeu bem', N'Dormiu 40 min', N'Feliz',   N'Foi ao banheiro 2x', N'Ajudou os colegas no alfabeto móvel.'),
    -- 13/11
    (2, 1, 1, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'3 fraldas trocadas', N'Acompanhou os chocalhos e movimentou os braços no ritmo da música.'),
    (2, 3, 1, N'Comeu bem', N'Dormiu 1h10', N'Tranquilo', N'2 fraldas trocadas', N'Observou a música e explorou o chocalho com as duas mãos.'),
    (2, 8, 1, N'Comeu bem', N'Dormiu 1h20', N'Feliz', N'2 fraldas trocadas', N'Interagiu com os colegas durante a atividade musical.'),
    (7, 4, 2, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'Foi ao banheiro 2x', N'Explorou a massinha e criou formas simples.'),
    (7, 5, 2, N'Comeu bem', N'Dormiu 1h', N'Tranquilo', N'Foi ao banheiro 2x', N'Modelou bolinhas e cobrinhas com interesse.'),
    (12, 2, 3, N'Comeu bem', N'Não dormiu', N'Feliz', N'Foi ao banheiro 2x', N'Participou do plantio e fez um desenho do feijão.'),
    (12, 6, 3, N'Comeu bem', N'Dormiu 40 min', N'Tranquilo', N'Foi ao banheiro 1x', N'Plantou o feijão e registrou sua expectativa por desenho.'),
    (12, 7, 3, N'Comeu bem', N'Dormiu 45 min', N'Feliz', N'Foi ao banheiro 2x', N'Ajudou a organizar os materiais do plantio.'),
    -- 16/11
    (3, 1, 1, N'Comeu bem', N'Dormiu 1h20', N'Feliz', N'3 fraldas trocadas', N'Acompanhou as bolhas com o olhar e tentou alcançá-las.'),
    (3, 8, 1, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'2 fraldas trocadas', N'Riu e tentou tocar as bolhas durante a brincadeira.'),
    (8, 4, 2, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'Foi ao banheiro 2x', N'Concluiu o circuito com apoio e equilíbrio.'),
    (8, 5, 2, N'Comeu bem', N'Dormiu 1h10', N'Agitado', N'Foi ao banheiro 2x', N'Participou do percurso, pulando e rastejando pelos obstáculos.'),
    (8, 9, 2, N'Comeu bem', N'Dormiu 1h', N'Agitado', N'Foi ao banheiro 3x', N'Completou o circuito e respeitou a vez dos colegas.'),
    (13, 6, 3, N'Comeu bem', N'Dormiu 30 min', N'Tranquilo', N'Foi ao banheiro 1x', N'Classificou as peças por cor e forma.'),
    (13, 7, 3, N'Comeu bem', N'Dormiu 40 min', N'Feliz', N'Foi ao banheiro 2x', N'Identificou formas e ajudou o grupo na classificação.'),
    -- 17/11
    (4, 1, 1, N'Comeu bem', N'Dormiu 1h30', N'Tranquilo', N'3 fraldas trocadas', N'Engatinhou pelo circuito e alcançou os brinquedos de apoio.'),
    (4, 3, 1, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'2 fraldas trocadas', N'Explorou as almofadas e praticou o equilíbrio com apoio.'),
    (9, 5, 2, N'Comeu bem', N'Dormiu 1h', N'Tranquilo', N'Foi ao banheiro 2x', N'Cantou e acompanhou as músicas com gestos.'),
    (9, 9, 2, N'Comeu bem', N'Dormiu 1h15', N'Feliz', N'Foi ao banheiro 3x', N'Participou das cantigas e tocou os instrumentos no ritmo.'),
    (14, 2, 3, N'Comeu bem', N'Não dormiu', N'Feliz', N'Foi ao banheiro 2x', N'Assistiu ao teatro e recontou uma parte da história.'),
    (14, 6, 3, N'Comeu bem', N'Dormiu 35 min', N'Tranquilo', N'Foi ao banheiro 1x', N'Contribuiu com ideias durante a recontagem dos fantoches.'),
    (14, 7, 3, N'Comeu bem', N'Dormiu 40 min', N'Feliz', N'Foi ao banheiro 2x', N'Participou da história e ajudou a guardar os fantoches.'),
    -- 18/11
    (5, 1, 1, N'Comeu bem', N'Dormiu 1h20', N'Tranquilo', N'3 fraldas trocadas', N'Tocou as figuras do livro de pano durante a leitura.'),
    (5, 3, 1, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'2 fraldas trocadas', N'Observou as cores e apontou figuras no livro.'),
    (5, 8, 1, N'Comeu bem', N'Dormiu 1h10', N'Feliz', N'2 fraldas trocadas', N'Escolheu um livro e explorou suas imagens e texturas.'),
    (10, 4, 2, N'Comeu bem', N'Dormiu 1h', N'Feliz', N'Foi ao banheiro 2x', N'Participou do faz de conta e organizou as frutas do mercadinho.'),
    (10, 9, 2, N'Comeu bem', N'Dormiu 1h10', N'Tranquilo', N'Foi ao banheiro 3x', N'Brincou de comprar e vender frutas com os colegas.'),
    (15, 2, 3, N'Comeu bem', N'Não dormiu', N'Feliz', N'Foi ao banheiro 2x', N'Criou desenhos e escolheu materiais para a colagem coletiva.'),
    (15, 7, 3, N'Comeu bem', N'Dormiu 40 min', N'Feliz', N'Foi ao banheiro 2x', N'Colaborou no painel coletivo com desenho e colagem.');

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Conferencia rapida
SELECT ap.data_aula, p.nome AS professor, t.nome_turma, ap.atividade_dinamica
FROM dbo.aulas_planejamentos AS ap
JOIN dbo.pessoas AS p ON p.codigo = ap.professor_codigo
JOIN dbo.turmas  AS t ON t.codigo = ap.turma_codigo
ORDER BY p.nome, ap.data_aula;

SELECT * FROM dbo.vw_diario_bordo_aluno ORDER BY turma_codigo, aluno_nome;
GO