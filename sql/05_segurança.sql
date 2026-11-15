USE oficina_mecanica;

-- 1. Papéis
CREATE ROLE IF NOT EXISTS papel_admin, papel_operador, papel_consulta;

-- 2. Views que escondem dados sensíveis (antes dos GRANTs)
CREATE OR REPLACE VIEW vw_clientes_publico AS
SELECT id_cliente, nome, cidade, estado FROM cliente;
-- CPF, CNPJ, endereço completo, telefone e e-mail ficam de fora.

-- 3. Privilégios
GRANT ALL PRIVILEGES ON oficina_mecanica.* TO papel_admin;

-- Operador: cadastra e altera, mas não exclui
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.cliente          TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.cliente_telefone TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.cliente_email    TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.veiculo          TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.ordem_servico    TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.os_servico       TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.os_peca          TO papel_operador;
GRANT SELECT, INSERT, UPDATE ON oficina_mecanica.os_mecanico      TO papel_operador;
GRANT SELECT, INSERT         ON oficina_mecanica.historico_os     TO papel_operador;
GRANT SELECT, INSERT         ON oficina_mecanica.pagamento        TO papel_operador;
GRANT SELECT ON oficina_mecanica.servico   TO papel_operador;
GRANT SELECT ON oficina_mecanica.peca      TO papel_operador;
GRANT SELECT ON oficina_mecanica.atendente TO papel_operador;
GRANT SELECT ON oficina_mecanica.mecanico  TO papel_operador;

-- Consulta: só as views
GRANT SELECT ON oficina_mecanica.vw_clientes_publico TO papel_consulta;
GRANT SELECT ON oficina_mecanica.vw_relatorio_os     TO papel_consulta;

-- 4. Usuários de exemplo (troque as senhas)
CREATE USER IF NOT EXISTS 'op_oficina'@'localhost'  IDENTIFIED BY 'Troque#Senha1';
CREATE USER IF NOT EXISTS 'con_oficina'@'localhost' IDENTIFIED BY 'Troque#Senha2';
GRANT papel_operador TO 'op_oficina'@'localhost';
GRANT papel_consulta TO 'con_oficina'@'localhost';
SET DEFAULT ROLE papel_operador TO 'op_oficina'@'localhost';
SET DEFAULT ROLE papel_consulta TO 'con_oficina'@'localhost';

-- 5. Auditoria
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    tabela_afetada VARCHAR(50) NOT NULL,
    operacao VARCHAR(10) NOT NULL,
    usuario_bd VARCHAR(100) NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dados_anteriores TEXT,
    dados_posteriores TEXT,
    CONSTRAINT ck_auditoria_operacao CHECK (operacao IN ('INSERT', 'UPDATE', 'DELETE'))
);

DROP TRIGGER IF EXISTS trg_audita_cliente_update;
DROP TRIGGER IF EXISTS trg_audita_cliente_delete;

DELIMITER //
CREATE TRIGGER trg_audita_cliente_update
AFTER UPDATE ON cliente
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (tabela_afetada, operacao, usuario_bd, dados_anteriores, dados_posteriores)
    VALUES ('cliente', 'UPDATE', CURRENT_USER(),
            CONCAT_WS(' | ', CONCAT('ID: ', OLD.id_cliente), CONCAT('Nome: ', OLD.nome), CONCAT('Cidade: ', OLD.cidade)),
            CONCAT_WS(' | ', CONCAT('ID: ', NEW.id_cliente), CONCAT('Nome: ', NEW.nome), CONCAT('Cidade: ', NEW.cidade)));
END//

CREATE TRIGGER trg_audita_cliente_delete
AFTER DELETE ON cliente
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (tabela_afetada, operacao, usuario_bd, dados_anteriores, dados_posteriores)
    VALUES ('cliente', 'DELETE', CURRENT_USER(),
            CONCAT_WS(' | ', CONCAT('ID: ', OLD.id_cliente), CONCAT('Nome: ', OLD.nome)),
            NULL);
END//
DELIMITER ;