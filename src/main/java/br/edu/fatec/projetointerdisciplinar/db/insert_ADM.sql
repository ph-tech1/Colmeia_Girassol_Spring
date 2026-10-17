INSERT INTO dbo.pessoas (
    nome, email, cpf, rg, data_nascimento, senha, tipo_perfil,
    cep, cidade, uf, endereco, telefone
)
VALUES (
    'Nome do Administrador',
    'admin@escola.com',
    '12345678901',               -- CPF com 11 dígitos
    NULL,                        -- RG (ou informe o RG)
    '1990-01-01',                -- Data de nascimento (AAAA-MM-DD)
    '$2a$10$1E8zxFzJPY5mbqO3wyTap.vEzu4P4u5qvsL6rTKab9639F7MRTqmG',
    'ADMIN',
    '01001000',                  -- CEP com 8 dígitos
    'São Paulo',
    'SP',
    'Rua Exemplo, 123',
    '11999999999'
);
