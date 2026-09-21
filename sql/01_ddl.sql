-- Criação do banco
DROP DATABASE IF EXISTS oficina_mecanica;
CREATE DATABASE oficina_mecanica DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oficina_mecanica;

-- Tabela Cliente (Engloba Pessoa Física e Jurídica - Decisão do Modelo Lógico)
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
    -- RN01 e Validação de Especialização: Garante a consistência dos dados de PF e PJ
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
    CONSTRAINT fk_telefone_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Tabela Multivalorada: E-mail do Cliente
CREATE TABLE cliente_email (
    endereco_email VARCHAR(100) NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT pk_cliente_email PRIMARY KEY (endereco_email, id_cliente),
    CONSTRAINT fk_email_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Tabela Funcionário (Engloba Mecânicos e Atendentes com hierarquia)
CREATE TABLE funcionario (
    id_funcionario INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    tipo_funcionario VARCHAR(20) NOT NULL,
    id_supervisor INT NULL,
    CONSTRAINT pk_funcionario PRIMARY KEY (id_funcionario),
    -- RN16: Diferenciar funções
    CONSTRAINT ck_funcionario_tipo CHECK (tipo_funcionario IN ('MECANICO', 'ATENDENTE')),
    -- RN17: Mecânico possui no máximo um supervisor (Autorrelacionamento)
    CONSTRAINT fk_funcionario_supervisor FOREIGN KEY (id_supervisor) 
        REFERENCES funcionario(id_funcionario) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Tabela Serviço (Catálogo)
CREATE TABLE servico (
    id_servico INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    valor_referencia DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    -- RN12: Serviço possui valor de referência maior que zero
    CONSTRAINT ck_servico_valor CHECK (valor_referencia > 0)
);

-- Tabela Peça (Catálogo)
CREATE TABLE peca (
    id_peca INT AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    CONSTRAINT pk_peca PRIMARY KEY (id_peca)
);

-- TABELAS DEPENDENTES (RELAÇÕES CORE)
-- Tabela Veículo
CREATE TABLE veiculo (
    id_veiculo INT AUTO_INCREMENT,
    modelo VARCHAR(100) NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT pk_veiculo PRIMARY KEY (id_veiculo),
    -- RN01 e RN03: Veículo associado a um cliente (Restringe a deleção do cliente se houver veículos)
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (id_cliente) 
        REFERENCES cliente(id_cliente) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Tabela Ordem de Serviço
CREATE TABLE ordem_servico (
    id_ordem_servico INT AUTO_INCREMENT,
    data_abertura DATE NOT NULL,
    data_fechamento DATE NULL,
    situacao VARCHAR(20) NOT NULL,
    id_veiculo INT NOT NULL,
    id_os_garantia INT NULL,
    CONSTRAINT pk_ordem_servico PRIMARY KEY (id_ordem_servico),
    -- RN04: Situações válidas da OS
    CONSTRAINT ck_os_situacao CHECK (situacao IN ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECAS', 'CONCLUIDA', 'CANCELADA')),
    -- RN06: Data de fechamento condicional
    CONSTRAINT ck_os_datas CHECK (
        (situacao IN ('CONCLUIDA', 'CANCELADA') AND data_fechamento IS NOT NULL AND data_fechamento >= data_abertura) OR
        (situacao NOT IN ('CONCLUIDA', 'CANCELADA') AND data_fechamento IS NULL)
    ),
    -- RN03: Associada a um veículo
    CONSTRAINT fk_os_veiculo FOREIGN KEY (id_veiculo) 
        REFERENCES veiculo(id_veiculo) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN19: Relacionamento de garantia com ordem original
    CONSTRAINT fk_os_garantia FOREIGN KEY (id_os_garantia) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Entidade Fraca: Histórico da OS
CREATE TABLE historico_os (
    data_hora DATETIME NOT NULL,
    id_ordem_servico INT NOT NULL,
    situacao VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    CONSTRAINT pk_historico_os PRIMARY KEY (data_hora, id_ordem_servico),
    -- RN05: Registro do momento da mudança de status, atrelado à OS
    CONSTRAINT fk_historico_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_historico_situacao CHECK (situacao IN ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECAS', 'CONCLUIDA', 'CANCELADA'))
);

-- 3. TABELAS ASSOCIATIVAS
-- Tabela OS_Servico (Relacionamento N:N com atributos - Preço Congelado)

CREATE TABLE os_servico (
    id_ordem_servico INT NOT NULL,
    id_servico INT NOT NULL,
    quantidade INT NOT NULL,
    valor_cobrado DECIMAL(10,2) NOT NULL,
    horas_mao_obra DECIMAL(5,2) NOT NULL,
    tempo_garantia_meses INT NULL,
    CONSTRAINT pk_os_servico PRIMARY KEY (id_ordem_servico, id_servico),
    CONSTRAINT fk_os_serv_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_os_serv_servico FOREIGN KEY (id_servico) 
        REFERENCES servico(id_servico) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- RN08: Quantidade e mão de obra válidas
    CONSTRAINT ck_os_serv_qtd CHECK (quantidade > 0 AND horas_mao_obra >= 0)
);

-- Tabela OS_Peca (Relacionamento N:N com atributos - Preço Congelado)
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
    -- RN10 e RN11: Valor e quantidade no momento da OS
    CONSTRAINT ck_os_peca_qtd CHECK (quantidade > 0 AND valor_unitario >= 0)
);

-- Tabela OS_Funcionario (Relacionamento N:N)
CREATE TABLE os_funcionario (
    id_ordem_servico INT NOT NULL,
    id_funcionario INT NOT NULL,
    CONSTRAINT pk_os_funcionario PRIMARY KEY (id_ordem_servico, id_funcionario),
    -- RN14 e RN15: Associação de funcionários à execução da OS
    CONSTRAINT fk_os_func_os FOREIGN KEY (id_ordem_servico) 
        REFERENCES ordem_servico(id_ordem_servico) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_os_func_func FOREIGN KEY (id_funcionario) 
        REFERENCES funcionario(id_funcionario) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 4. ÍNDICES DE DESEMPENHO (Projeto Físico)

CREATE INDEX idx_os_data_abertura ON ordem_servico(data_abertura);
CREATE INDEX idx_veiculo_id_cliente ON veiculo(id_cliente);
CREATE INDEX idx_cliente_nome ON cliente(nome);