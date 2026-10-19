-- Criação dos papéis (perfis)

CREATE ROLE papel_admin;
CREATE ROLE papel_operador;
CREATE ROLE papel_consulta;

-- Concessão de privilégios para o operador (exemplo em tabelas transacionais)
GRANT SELECT, INSERT, UPDATE ON tb_cliente, tb_transacao TO papel_operador;
REVOKE DELETE ON tb_cliente, tb_transacao FROM papel_operador;

-- Concessão de privilégios mínimos para consulta
GRANT SELECT ON vw_dados_publicos TO papel_consulta;
--====================

--====================
-- Criação de Visão para Restrição de Dados
CREATE VIEW vw_dados_publicos AS
SELECT id_cliente, nome, data_cadastro
FROM tb_cliente;
-- Note que colunas sensíveis como CPF, e-mail e telefone foram omitidas para proteger o usuário.
--====================

--====================
-- Estrutura da Tabela de Auditoria

CREATE TABLE tb_auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    tabela_afetada VARCHAR(50) NOT NULL,
    operacao VARCHAR(10) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    usuario_bd VARCHAR(100) NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dados_anteriores TEXT,
    dados_posteriores TEXT
);
--====================

--====================
-- Auditoria de Alteração Cadastral de Cliente (trg_audita_cliente_update)
DELIMITER //
CREATE TRIGGER trg_audita_cliente_update
AFTER UPDATE ON tb_cliente
FOR EACH ROW
BEGIN
    INSERT INTO tb_auditoria (tabela_afetada, operacao, usuario_bd, dados_anteriores, dados_posteriores)
    VALUES (
        'tb_cliente',
        'UPDATE',
        CURRENT_USER(),
        CONCAT('Nome: ', OLD.nome, ' | CPF: ', OLD.cpf),
        CONCAT('Nome: ', NEW.nome, ' | CPF: ', NEW.cpf)
    );
END//
DELIMITER ;
--====================

--====================
-- Auditoria de Exclusão de Registros Sensíveis (trg_audita_cliente_delete)
DELIMITER //
CREATE TRIGGER trg_audita_cliente_delete
AFTER DELETE ON tb_cliente
FOR EACH ROW
BEGIN
    INSERT INTO tb_auditoria (tabela_afetada, operacao, usuario_bd, dados_anteriores, dados_posteriores)
    VALUES (
        'tb_cliente',
        'DELETE',
        CURRENT_USER(),
        CONCAT('ID Deletado: ', OLD.id_cliente, ' | Nome: ', OLD.nome),
        NULL
    );
END//
DELIMITER ;
--====================