INSERT INTO dbo.pessoas (
    nome, email, cpf, rg, data_nascimento, sexo, senha, tipo_perfil,
    cep, cidade, uf, bairro, endereco, numero, complemento, telefone
)
VALUES (
    'Nome do Administrador',
    'admin@escola.com',
    '12345678901',               -- CPF com 11 dígitos
    NULL,                        -- RG (ou informe o RG)
    '1990-01-01',                -- Data de nascimento (AAAA-MM-DD)
    'O',                         -- Sexo: F, M ou O
    '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG',
    'ADMIN',
    '01001000',                  -- CEP com 8 dígitos
    'São Paulo',
    'SP',
    'Centro',
    'Rua Exemplo',
    '123',
    NULL,
    '11999999999'
);
