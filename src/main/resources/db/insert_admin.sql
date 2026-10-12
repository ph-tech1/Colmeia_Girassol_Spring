-- SQL Server: preencha os valores antes de executar.
-- @senha deve receber o hash BCrypt da senha (nunca a senha em texto puro).
-- Gere esse valor em Java com: new BCryptPasswordEncoder().encode("senha-temporaria")
SET XACT_ABORT ON;

DECLARE @nome VARCHAR(100) = NULL;
DECLARE @email VARCHAR(100) = NULL;
DECLARE @cpf VARCHAR(11) = NULL;
DECLARE @rg VARCHAR(20) = NULL;
DECLARE @data_nascimento DATE = NULL;
DECLARE @senha VARCHAR(255) = NULL;
DECLARE @cep VARCHAR(8) = NULL;
DECLARE @cidade VARCHAR(50) = NULL;
DECLARE @uf CHAR(2) = NULL;
DECLARE @endereco VARCHAR(100) = NULL;
DECLARE @telefone VARCHAR(15) = NULL;

IF @nome IS NULL OR @email IS NULL OR @cpf IS NULL OR @data_nascimento IS NULL
   OR @senha IS NULL OR @cep IS NULL OR @cidade IS NULL OR @uf IS NULL
   OR @endereco IS NULL OR @telefone IS NULL
    THROW 50001, 'Preencha todos os dados obrigatorios do administrador neste script.', 1;

IF LEN(@cpf) <> 11 OR LEN(@cep) <> 8
    THROW 50002, 'CPF deve conter 11 digitos e CEP deve conter 8 digitos.', 1;

IF EXISTS (SELECT 1 FROM dbo.pessoas WHERE cpf = @cpf OR email = @email)
    THROW 50003, 'Ja existe uma pessoa com este CPF ou e-mail.', 1;

INSERT INTO dbo.pessoas (
    nome, email, cpf, rg, data_nascimento, senha, tipo_perfil,
    cep, cidade, uf, endereco, telefone
)
VALUES (
    @nome, @email, @cpf, @rg, @data_nascimento, @senha, 'ADMIN',
    @cep, @cidade, @uf, @endereco, @telefone
);
