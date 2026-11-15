-- =====================================================================
-- PROJETO FINAL - LABORATÓRIO DE BANCO DE DADOS (ETAPA 2)
-- ARTEFATO B3: TRANSAÇÕES E CONTROLE DE CONCORRÊNCIA
-- Sistema: oficina_mecanica (MySQL 8.0+)
-- =====================================================================

USE oficina_mecanica;

-- =====================================================================
-- 1. OPERAÇÃO TRANSACIONAL MULTITABELA (CENÁRIO DE SUCESSO - COMMIT)
-- Operação: Conclusão de Ordem de Serviço com Pagamento e Histórico
-- =====================================================================

START TRANSACTION;

-- Passo 1: Atualizar o status e data de fechamento da Ordem de Serviço #36
UPDATE ordem_servico
SET situacao = 'CONCLUIDA',
    data_fechamento = '2026-09-21'
WHERE id_ordem_servico = 36
  AND situacao = 'EM_EXECUCAO';

-- Passo 2: Registrar o pagamento recebido referente à OS #36
INSERT INTO pagamento (id_ordem_servico, valor_total, forma_pagamento, data_pagamento)
VALUES (36, 330.00, 'PIX', '2026-09-21 10:30:00');

-- Passo 3: Inserir o registro cronológico no histórico (Entidade Fraca)
INSERT INTO historico_os (data_hora, id_ordem_servico, situacao, descricao)
VALUES ('2026-09-21 10:30:00', 36, 'CONCLUIDA', 'Serviço finalizado e pagamento confirmado via PIX.');

-- Confirmação permanente de todas as alterações
COMMIT;


-- =====================================================================
-- 2. CENÁRIO DE FALHA PROVOCADO E ROLLBACK (TESTE DE ATOMICIDADE)
-- =====================================================================

START TRANSACTION;

-- Passo 1: Atualizar o status da OS #37 (Executado com sucesso no escopo da transação)
UPDATE ordem_servico
SET situacao = 'CONCLUIDA',
    data_fechamento = '2026-09-21'
WHERE id_ordem_servico = 37
  AND situacao = 'EM_EXECUCAO';

-- Passo 2: Tentativa de inserir pagamento com FORMA DE PAGAMENTO INVÁLIDA ('FIADO')
-- Esta instrução dispara o ERRO 3819 (HY000): Check constraint 'ck_pagamento_forma' is violated.
INSERT INTO pagamento (id_ordem_servico, valor_total, forma_pagamento, data_pagamento)
VALUES (37, 120.00, 'FIADO', '2026-09-21 10:35:00');

-- Ocorrendo a exceção no Passo 2, executa-se o ROLLBACK para manter o banco íntegro
ROLLBACK;

-- Consulta de verificação de integridade pós-Rollback (A OS #37 permanece intacta no estado original)
SELECT id_ordem_servico, situacao, data_fechamento 
FROM ordem_servico 
WHERE id_ordem_servico = 37;

-- =====================================================================
-- 3. EXPERIMENTO PRÁTICO DE CONCORRÊNCIA (DUAS SESSÕES SIMULTÂNEAS)
-- Nível de Isolamento do InnoDB: REPEATABLE READ
-- =====================================================================

-- ---------------------------------------------------------------------
-- SESSÃO 1 (TERMINAL A - Atendente Juliana)
-- ---------------------------------------------------------------------
-- Execute os passos NA ORDEM, alternando entre os dois terminais.

-- [S1] Passo 1: abre a transação
START TRANSACTION;
-- [S1] Passo 2: X-lock na linha da OS #38
UPDATE ordem_servico SET situacao = 'EM_EXECUCAO' WHERE id_ordem_servico = 38;

-- [S2] Passo 3: leitura consistente (não bloqueia, REPEATABLE READ): ainda mostra 'ABERTA'
START TRANSACTION;
SELECT id_ordem_servico, situacao FROM ordem_servico WHERE id_ordem_servico = 38;

-- [S2] Passo 4: tenta alterar a mesma linha: FICA BLOQUEADA aguardando a Sessão 1
UPDATE ordem_servico
SET situacao = 'CANCELADA', data_fechamento = '2026-09-21'
WHERE id_ordem_servico = 38 AND situacao = 'ABERTA';

-- [S1] Passo 5: registra no histórico e libera o bloqueio
INSERT INTO historico_os (data_hora, id_ordem_servico, situacao, descricao)
VALUES ('2026-09-21 11:00:00', 38, 'EM_EXECUCAO', 'Iniciado atendimento técnico');
COMMIT;

-- [S2] Passo 6: o UPDATE do passo 4 é liberado, mas afeta 0 linhas
-- (a OS já não está 'ABERTA'), o que evita a atualização perdida.
SELECT id_ordem_servico, situacao FROM ordem_servico WHERE id_ordem_servico = 38;
ROLLBACK;
