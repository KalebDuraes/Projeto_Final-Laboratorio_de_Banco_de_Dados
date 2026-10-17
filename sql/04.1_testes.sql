-- ============================================================
-- PROJETO FINAL: OFICINA MECANICA
-- B4 - DIAGNOSTICO E TESTES COMPARATIVOS
-- Arquivo: 04.1_testes.sql
--
-- IMPORTANTE:
-- Os cenarios de referencia usam IGNORE INDEX.
-- Eles nao recuperam os planos historicos anteriores
-- a criacao dos indices.
--
-- Este arquivo nao cria nem remove indices.
-- Executar cada bloco EXPLAIN ANALYZE separadamente.
-- ============================================================

USE oficina_mecanica;


-- ============================================================
-- TESTE 1: INDICES EXISTENTES
-- ============================================================

SHOW INDEX FROM ordem_servico;
SHOW INDEX FROM os_servico;
SHOW INDEX FROM servico;
SHOW INDEX FROM pagamento;


-- ============================================================
-- TESTE 2: VOLUME DE DADOS
-- ============================================================

SELECT 'ordem_servico' AS tabela, COUNT(*) AS quantidade
FROM ordem_servico
UNION ALL
SELECT 'os_servico', COUNT(*) FROM os_servico
UNION ALL
SELECT 'servico', COUNT(*) FROM servico
UNION ALL
SELECT 'pagamento', COUNT(*) FROM pagamento;


-- ============================================================
-- TESTE 3: ESTRUTURA DAS TABELAS
-- ============================================================

SHOW CREATE TABLE ordem_servico;
SHOW CREATE TABLE os_servico;
SHOW CREATE TABLE servico;
SHOW CREATE TABLE pagamento;


-- ============================================================
-- CONSULTA 1 - CENARIO DE REFERENCIA
-- Ignora o indice composto experimental.
-- O indice idx_os_data_abertura continua disponivel.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    id_ordem_servico,
    data_abertura,
    situacao,
    id_veiculo
FROM ordem_servico
    IGNORE INDEX (idx_os_situacao_fechamento_abertura)
WHERE situacao IN (
    'ABERTA',
    'EM_EXECUCAO',
    'AGUARDANDO_PECAS'
)
AND data_fechamento IS NULL
AND data_abertura >= '2026-09-17'
AND data_abertura < '2026-09-21';


-- ============================================================
-- CONSULTA 1 - CENARIO ALTERNATIVO
-- Ignora o indice de data para observar o uso do composto.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    id_ordem_servico,
    data_abertura,
    situacao,
    id_veiculo
FROM ordem_servico
    IGNORE INDEX (idx_os_data_abertura)
WHERE situacao IN (
    'ABERTA',
    'EM_EXECUCAO',
    'AGUARDANDO_PECAS'
)
AND data_fechamento IS NULL
AND data_abertura >= '2026-09-17'
AND data_abertura < '2026-09-21';


-- ============================================================
-- CONSULTA 2 - CENARIO DE REFERENCIA
-- Ignora o indice experimental de os_servico.
-- A chave primaria continua disponivel.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    s.id_servico,
    s.nome,
    COUNT(DISTINCT oss.id_ordem_servico) AS quantidade_de_os
FROM servico AS s
INNER JOIN os_servico AS oss
    IGNORE INDEX (idx_oss_servico_ordem)
    ON oss.id_servico = s.id_servico
GROUP BY
    s.id_servico,
    s.nome
HAVING COUNT(DISTINCT oss.id_ordem_servico) > 5
ORDER BY
    quantidade_de_os DESC,
    s.nome ASC;


-- ============================================================
-- CONSULTA 2 - CENARIO COM INDICE DISPONIVEL
-- O otimizador pode escolher o indice experimental.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    s.id_servico,
    s.nome,
    COUNT(DISTINCT oss.id_ordem_servico) AS quantidade_de_os
FROM servico AS s
INNER JOIN os_servico AS oss
    ON oss.id_servico = s.id_servico
GROUP BY
    s.id_servico,
    s.nome
HAVING COUNT(DISTINCT oss.id_ordem_servico) > 5
ORDER BY
    quantidade_de_os DESC,
    s.nome ASC;


-- ============================================================
-- CONSULTA 3 - CENARIO DE REFERENCIA
-- Ignora o indice experimental de pagamento.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    forma_pagamento,
    COUNT(*) AS quantidade_pagamentos,
    SUM(valor_total) AS total_recebido
FROM pagamento
    IGNORE INDEX (idx_pagamento_forma_data)
WHERE forma_pagamento = 'PIX'
  AND data_pagamento >= '2026-01-01 00:00:00'
  AND data_pagamento < '2027-01-01 00:00:00'
GROUP BY forma_pagamento;


-- ============================================================
-- CONSULTA 3 - CENARIO COM INDICE DISPONIVEL
-- O otimizador pode escolher o indice experimental.
-- ============================================================

EXPLAIN ANALYZE
SELECT
    forma_pagamento,
    COUNT(*) AS quantidade_pagamentos,
    SUM(valor_total) AS total_recebido
FROM pagamento
WHERE forma_pagamento = 'PIX'
  AND data_pagamento >= '2026-01-01 00:00:00'
  AND data_pagamento < '2027-01-01 00:00:00'
GROUP BY forma_pagamento;


-- ============================================================
-- FIM DOS TESTES
--
-- Guardar os planos completos e as evidencias das execucoes.
-- Nao afirmar ganho sem comparar os resultados medidos.
-- ============================================================
