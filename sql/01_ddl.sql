-- =====================================================================
-- PROJETO FINAL - LABORATÓRIO DE BANCO DE DADOS (ETAPA 2)
-- Script 01: DDL (Estrutura do Banco de Dados Atualizada e Corrigida)
-- =====================================================================

-- Criação do banco
DROP DATABASE IF EXISTS oficina_mecanica;
CREATE DATABASE oficina_mecanica DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oficina_mecanica;

-- 1. Tabela Cliente (Entidade Regular 1)
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cep CHAR(8) NOT NULL,
    estado CHAR(2) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    logradouro VARCHAR(100) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    complemento VARCHAR(50) NULL,
    tipo_pessoa CHAR(1) NOT NULL,
    cpf CHAR(11) NULL,
    cnpj CHAR(14) NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_cpf UNIQUE (cpf),
    CONSTRAINT uq_cliente_cnpj UNIQUE (cnpj),
    -- RN01: Garante a consistência dos dados de PF e PJ (Especialização)
    CONSTRAINT ck_cliente_tipo CHECK (tipo_pessoa IN ('F', 'J')),
    CONSTRAINT ck_cliente_doc CHECK (
        (tipo_pessoa = 'F' AND cpf IS NOT NULL AND cnpj IS NULL) OR 
        (tipo_pessoa = 'J' AND cnpj IS NOT NULL AND cpf IS NULL)
    )
);

-- Tabela Multivalorada: Telefone do Cliente
CREATE TABLE cliente_telefone (
    numero_telefone VARCHAR(20) NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT pk_cliente_telefone PRIMARY KEY (numero_telefone, id_cliente),
    -- RN02: Formato e obrigatoriedade de contato telefônico do cliente
    CONSTRAINT fk_telefone_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Tabela Multivalorada: E-mail do Cliente
CREATE TABLE cliente_email (
    endereco_email VARCHAR(100) NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT pk_cliente_email PRIMARY KEY (endereco_email, id_cliente),
    -- RN02: Validação e registro de e-mails do cliente
    CONSTRAINT fk_email_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 2. Tabela Atendente (Entidade Regular 2 - Papel Próprio)
CREATE TABLE atendente (
    id_atendente INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL,
    CONSTRAINT pk_atendente PRIMARY KEY (id_atendente),
    CONSTRAINT uq_atendente_cpf UNIQUE (cpf)
);

-- 3. Tabela Mecânico (Entidade Regular 3 - Papel Próprio com Autorrelacionamento)
CREATE TABLE mecanico (
    id_mecanico INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL,
    id_supervisor INT NULL,
    CONSTRAINT pk_mecanico PRIMARY KEY (id_mecanico),
    CONSTRAINT uq_mecanico_cpf UNIQUE (cpf),
    -- RN17: Mecânico possui no máximo um supervisor (Autorrelacionamento hierárquico)
    CONSTRAINT fk_mecanico_supervisor FOREIGN KEY (id_supervisor) 
        REFERENCES mecanico(id_mecanico) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- 4. Tabela Serviço (Entidade Regular 4 - Catálogo)
CREATE TABLE servico (
    id_servico INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    valor_referencia DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    -- RN12: Serviço possui valor de referência maior que zero
    CONSTRAINT ck_servico_valor CHECK (valor_referencia > 0)
);

-- 5. Tabela Peça (Entidade Regular 5 - Catálogo)
CREATE TABLE peca (
    id_peca INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    preco_unitario_sugerido DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_peca PRIMARY KEY (id_peca),
    -- RN09: Validação do preço sugerido da peça em estoque
    CONSTRAINT ck_peca_preco CHECK (preco_unitario_sugerido >= 0)
);

-- 6. Tabela Veículo (Entidade Regular 6)
CREATE TABLE veiculo (
    id_veiculo INT AUTO_INCREMENT,
    modelo VARCHAR(100) NOT NULL,
    placa VARCHAR(10) NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT pk_veiculo PRIMARY KEY (id_veiculo),
    CONSTRAINT uq_veiculo_placa UNIQUE (placa),
    -- RN03: Veículo associado obrigatoriamente a um cliente cadastrado
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 7. Tabela Ordem de Serviço (Entidade Regular 7)
CREATE TABLE ordem_servico (
    id_ordem_servico INT AUTO_INCREMENT,
    data_abertura DATE NOT NULL,
    data_fechamento DATE NULL,
    situacao VARCHAR(20) NOT NULL,
    id_veiculo INT NOT NULL,
    id_atendente INT NOT NULL,
    id_os_garantia INT NULL,
    CONSTRAINT pk_ordem_servico PRIMARY KEY (id_ordem_servico),
    -- RN04: Situações válidas da OS no ciclo de vida
    CONSTRAINT ck_os_situacao CHECK (situacao IN ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECAS', 'CONCLUIDA', 'CANCELADA')),
    -- RN06: Data de fechamento condicional à conclusão/cancelamento
    CONSTRAINT ck_os_datas CHECK (
        (situacao IN ('CONCLUIDA', 'CANCELADA') AND data_fechamento IS NOT NULL AND data_fechamento >= data_abertura) OR
        (situacao NOT IN ('CONCLUIDA', 'CANCELADA') AND data_fechamento IS NULL)
    ),
    -- RN18: Toda OS deve ser registrada e aberta por um atendente responsável
    CONSTRAINT fk_os_atendente FOREIGN KEY (id_atendente) 
        REFERENCES atendente(id_atendente) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN03: Associada a um veículo válido
    CONSTRAINT fk_os_veiculo FOREIGN KEY (id_veiculo) 
        REFERENCES veiculo(id_veiculo) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN19: Relacionamento de garantia com ordem de serviço original
    CONSTRAINT fk_os_garantia FOREIGN KEY (id_os_garantia) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- 8. Tabela Pagamento (Entidade Regular 8 - Adicionada para atender à Etapa 2)
CREATE TABLE pagamento (
    id_pagamento INT AUTO_INCREMENT,
    id_ordem_servico INT NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    data_pagamento DATETIME NOT NULL,
    CONSTRAINT pk_pagamento PRIMARY KEY (id_pagamento),
    -- RN20: Cada pagamento deve estar vinculado a uma OS concluída e validada
    CONSTRAINT fk_pagamento_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_pagamento_valor CHECK (valor_total > 0),
    CONSTRAINT ck_pagamento_forma CHECK (forma_pagamento IN ('DINHEIRO', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX'))
);

-- Entidade Fraca: Histórico da OS
CREATE TABLE historico_os (
    data_hora DATETIME NOT NULL,
    id_ordem_servico INT NOT NULL,
    situacao VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    CONSTRAINT pk_historico_os PRIMARY KEY (data_hora, id_ordem_servico),
    -- RN05: Registro cronológico da mudança de status atrelado à OS
    CONSTRAINT fk_historico_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_historico_situacao CHECK (situacao IN ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECAS', 'CONCLUIDA', 'CANCELADA'))
);

-- =====================================================================
-- TABELAS ASSOCIATIVAS (RELACIONAMENTOS N:N)
-- =====================================================================

-- Tabela OS_Servico (Atributos: Preço congelado e tempo_garantia padronizado)
CREATE TABLE os_servico (
    id_ordem_servico INT NOT NULL,
    id_servico INT NOT NULL,
    quantidade INT NOT NULL,
    valor_cobrado DECIMAL(10,2) NOT NULL,
    horas_mao_obra DECIMAL(5,2) NOT NULL,
    tempo_garantia INT NULL, -- Corrigido para bater com o modelo lógico (tempo_garantia)
    CONSTRAINT pk_os_servico PRIMARY KEY (id_ordem_servico, id_servico),
    CONSTRAINT fk_os_serv_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_os_serv_servico FOREIGN KEY (id_servico) 
        REFERENCES servico(id_servico) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN08 & RN13: Quantidade, mão de obra e regras de garantia válidas
    CONSTRAINT ck_os_serv_qtd CHECK (quantidade > 0 AND horas_mao_obra >= 0)
);

-- Tabela OS_Peca (Atributos: Preço unitário congelado no momento da OS)
CREATE TABLE os_peca (
    id_ordem_servico INT NOT NULL,
    id_peca INT NOT NULL,
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_os_peca PRIMARY KEY (id_ordem_servico, id_peca),
    CONSTRAINT fk_os_peca_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_os_peca_peca FOREIGN KEY (id_peca) 
        REFERENCES peca(id_peca) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN10 & RN11: Quantidade e valor unitário cobrado válidos
    CONSTRAINT ck_os_peca_qtd CHECK (quantidade > 0 AND valor_unitario >= 0)
);

-- Tabela OS_Mecanico (Associação N:N - Atribuição de mecânicos à execução da OS)
CREATE TABLE os_mecanico (
    id_ordem_servico INT NOT NULL,
    id_mecanico INT NOT NULL,
    CONSTRAINT pk_os_mecanico PRIMARY KEY (id_ordem_servico, id_mecanico),
    -- RN14 & RN15: Associação correta de mecânicos à execução técnica da OS
    CONSTRAINT fk_os_mec_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_os_mec_mecanico FOREIGN KEY (id_mecanico) 
        REFERENCES mecanico(id_mecanico) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- =====================================================================
-- ÍNDICES DE DESEMPENHO (Projeto Físico)
-- =====================================================================
CREATE INDEX idx_os_data_abertura ON ordem_servico(data_abertura);
CREATE INDEX idx_veiculo_id_cliente ON veiculo(id_cliente);
CREATE INDEX idx_cliente_nome ON cliente(nome);
