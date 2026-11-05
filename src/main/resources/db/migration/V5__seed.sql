-- Seed de teste (mesmos UUIDs do V9 do monolito). Senha dos funcionarios: Senha@123 (apenas o hash BCrypt).
INSERT INTO usuarios (id, status, nome, email, senha, cargo_enum)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ATIVO', 'Atendente Teste', 'atendente@mecanica.test', '$2a$10$fy/vj/iW/AukFhszMTR.7O5iJB707EfVk6zGsZEumVj4hmdgATyu6', 'ATENDENTE'),
    ('22222222-2222-2222-2222-222222222222', 'ATIVO', 'Mecanico Teste', 'mecanico@mecanica.test', '$2a$10$fy/vj/iW/AukFhszMTR.7O5iJB707EfVk6zGsZEumVj4hmdgATyu6', 'MECANICO'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ATIVO', 'Almoxarife Teste', 'almoxarife@mecanica.test', '$2a$10$fy/vj/iW/AukFhszMTR.7O5iJB707EfVk6zGsZEumVj4hmdgATyu6', 'ALMOXARIFE');

INSERT INTO clientes (id, status, nome, cpf, cnpj, email, telefone, usuario_id)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'ATIVO', 'Cliente PF Teste', '52998224725', NULL, 'cliente.pf@mecanica.test', '11987654321', '11111111-1111-1111-1111-111111111111'),
    ('33333333-3333-3333-3333-333333333334', 'ATIVO', 'Cliente PJ Teste', NULL, '11222333000181', 'cliente.pj@mecanica.test', '1133334444', '11111111-1111-1111-1111-111111111111');

INSERT INTO veiculos (id, status, placa, marca, modelo, ano, quantidade_eixos, cliente_id)
VALUES
    ('44444444-4444-4444-4444-444444444444', 'ATIVO', 'ABC1D23', 'Toyota', 'Corolla', '2020', 2, '33333333-3333-3333-3333-333333333333'),
    ('44444444-4444-4444-4444-444444444445', 'ATIVO', 'XYZ1234', 'Ford', 'Cargo', '2019', 3, '33333333-3333-3333-3333-333333333334');

