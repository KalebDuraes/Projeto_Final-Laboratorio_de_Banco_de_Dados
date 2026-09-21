USE oficina_mecanica;


-- ============================================================
-- CONSULTAS BÁSICAS
-- ============================================================


-- ============================================================
-- 1. PROJEÇÃO
-- Pergunta de negócio:
-- Quais são os nomes e tipos de todos os clientes cadastrados?
-- ============================================================

SELECT
    nome,
    tipo_pessoa
FROM cliente;


-- ============================================================
-- 2. WHERE
-- Pergunta de negócio:
-- Quais ordens de serviço estão atualmente em execução?
-- ============================================================

SELECT
    id_ordem_servico,
    data_abertura,
    situacao,
    id_veiculo
FROM ordem_servico
WHERE situacao = 'EM_EXECUCAO';


-- ============================================================
-- 3. ORDER BY
-- Pergunta de negócio:
-- Quais clientes estão cadastrados em ordem alfabética?
-- ============================================================

SELECT
    id_cliente,
    nome,
    tipo_pessoa
FROM cliente
ORDER BY nome ASC;


-- ============================================================
-- 4. LIKE
-- Pergunta de negócio:
-- Quais clientes possuem a palavra "Moreira" no nome?
-- ============================================================

SELECT
    id_cliente,
    nome,
    cidade,
    estado
FROM cliente
WHERE nome LIKE '%Moreira%';


-- ============================================================
-- 5. BETWEEN + IN + NULL
-- Pergunta de negócio:
-- Quais ordens abertas ou em execução, sem data de fechamento,
-- foram abertas entre 17/09/2026 e 20/09/2026?
-- ============================================================

SELECT
    id_ordem_servico,
    data_abertura,
    situacao,
    id_veiculo
FROM ordem_servico
WHERE data_abertura BETWEEN '2026-09-17' AND '2026-09-20'
  AND situacao IN ('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECAS')
  AND data_fechamento IS NULL;

-- ============================================================
-- CONSULTAS COM JOIN E AGREGAÇÃO
-- ============================================================


-- ============================================================
-- 6. JOIN COM 3 TABELAS
-- Pergunta de negócio:
-- Quais clientes possuem ordens de serviço e quais são os
-- respectivos veículos e situações das ordens?
-- ============================================================

SELECT
    c.nome AS cliente,
    v.modelo AS veiculo,
    os.id_ordem_servico,
    os.data_abertura,
    os.situacao
FROM cliente c
INNER JOIN veiculo v
    ON v.id_cliente = c.id_cliente
INNER JOIN ordem_servico os
    ON os.id_veiculo = v.id_veiculo
ORDER BY os.id_ordem_servico;


-- ============================================================
-- 7. LEFT JOIN
-- Pergunta de negócio:
-- Quais clientes estão cadastrados e quais veículos pertencem
-- a cada cliente, incluindo clientes que eventualmente não
-- possuam veículos?
-- ============================================================

SELECT
    c.id_cliente,
    c.nome AS cliente,
    v.id_veiculo,
    v.modelo AS veiculo
FROM cliente c
LEFT JOIN veiculo v
    ON v.id_cliente = c.id_cliente
ORDER BY c.id_cliente, v.id_veiculo;


-- ============================================================
-- 8. GROUP BY + HAVING
-- Pergunta de negócio:
-- Quais serviços foram utilizados em mais de 5 ordens de serviço?
-- ============================================================

SELECT
    s.id_servico,
    s.nome AS servico,
    COUNT(*) AS quantidade_de_os
FROM servico s
INNER JOIN os_servico os
    ON os.id_servico = s.id_servico
GROUP BY
    s.id_servico,
    s.nome
HAVING COUNT(*) > 5
ORDER BY quantidade_de_os DESC;


-- ============================================================
-- 9. AGREGAÇÃO POR CLIENTE
-- Pergunta de negócio:
-- Quais clientes possuem duas ou mais ordens de serviço?
-- ============================================================

SELECT
    c.id_cliente,
    c.nome AS cliente,
    COUNT(os.id_ordem_servico) AS quantidade_de_os
FROM cliente c
INNER JOIN veiculo v
    ON v.id_cliente = c.id_cliente
INNER JOIN ordem_servico os
    ON os.id_veiculo = v.id_veiculo
GROUP BY
    c.id_cliente,
    c.nome
HAVING COUNT(os.id_ordem_servico) >= 2
ORDER BY quantidade_de_os DESC;


-- ============================================================
-- 10. JOIN + AGREGAÇÃO
-- Pergunta de negócio:
-- Qual é o valor total dos serviços cobrados em cada ordem
-- de serviço?
-- ============================================================

SELECT
    os.id_ordem_servico,
    os.situacao,
    COALESCE(SUM(oss.quantidade * oss.valor_cobrado), 0) AS total_servicos
FROM ordem_servico os
LEFT JOIN os_servico oss
    ON oss.id_ordem_servico = os.id_ordem_servico
GROUP BY
    os.id_ordem_servico,
    os.situacao
ORDER BY total_servicos DESC;


-- ============================================================
-- CONSULTAS AVANÇADAS
-- ============================================================


-- ============================================================
-- 11. SUBCONSULTA CORRELACIONADA
-- Pergunta de negócio:
-- Quais utilizações de serviços possuem valor cobrado acima
-- da média cobrada para aquele mesmo serviço?
--
-- A subconsulta é correlacionada porque utiliza o
-- os1.id_servico da consulta externa.
-- ============================================================

SELECT
    os1.id_ordem_servico,
    s.nome AS servico,
    os1.valor_cobrado
FROM os_servico os1
INNER JOIN servico s
    ON s.id_servico = os1.id_servico
WHERE os1.valor_cobrado > (
    SELECT AVG(os2.valor_cobrado)
    FROM os_servico os2
    WHERE os2.id_servico = os1.id_servico
)
ORDER BY os1.id_servico, os1.valor_cobrado DESC;


-- ============================================================
-- 12. EXISTS
-- Pergunta de negócio:
-- Quais clientes possuem pelo menos uma ordem de serviço
-- que ainda não foi fechada?
-- ============================================================

SELECT
    c.id_cliente,
    c.nome
FROM cliente c
WHERE EXISTS (
    SELECT 1
    FROM veiculo v
    INNER JOIN ordem_servico os
        ON os.id_veiculo = v.id_veiculo
    WHERE v.id_cliente = c.id_cliente
      AND os.data_fechamento IS NULL
)
ORDER BY c.nome;


-- ============================================================
-- 13. NOT EXISTS
-- Pergunta de negócio:
-- Quais veículos ainda não possuem nenhuma ordem de serviço
-- cadastrada?
-- ============================================================

SELECT
    v.id_veiculo,
    v.modelo,
    v.id_cliente
FROM veiculo v
WHERE NOT EXISTS (
    SELECT 1
    FROM ordem_servico os
    WHERE os.id_veiculo = v.id_veiculo
)
ORDER BY v.id_veiculo;


-- ============================================================
-- 14. SUBCONSULTAS + REGRA DE NEGÓCIO
-- Pergunta de negócio:
-- Quais ordens de serviço possuem valor total de serviços
-- somado ao valor total das peças superior a R$ 500,00?
-- ============================================================

SELECT
    os.id_ordem_servico,
    os.situacao,

    COALESCE(
        (
            SELECT SUM(oss.quantidade * oss.valor_cobrado)
            FROM os_servico oss
            WHERE oss.id_ordem_servico = os.id_ordem_servico
        ),
        0
    ) AS total_servicos,

    COALESCE(
        (
            SELECT SUM(op.quantidade * op.valor_unitario)
            FROM os_peca op
            WHERE op.id_ordem_servico = os.id_ordem_servico
        ),
        0
    ) AS total_pecas,

    COALESCE(
        (
            SELECT SUM(oss.quantidade * oss.valor_cobrado)
            FROM os_servico oss
            WHERE oss.id_ordem_servico = os.id_ordem_servico
        ),
        0
    )
    +
    COALESCE(
        (
            SELECT SUM(op.quantidade * op.valor_unitario)
            FROM os_peca op
            WHERE op.id_ordem_servico = os.id_ordem_servico
        ),
        0
    ) AS valor_total

FROM ordem_servico os

WHERE
    COALESCE(
        (
            SELECT SUM(oss.quantidade * oss.valor_cobrado)
            FROM os_servico oss
            WHERE oss.id_ordem_servico = os.id_ordem_servico
        ),
        0
    )
    +
    COALESCE(
        (
            SELECT SUM(op.quantidade * op.valor_unitario)
            FROM os_peca op
            WHERE op.id_ordem_servico = os.id_ordem_servico
        ),
        0
    ) > 500

ORDER BY valor_total DESC;


-- ============================================================
-- 15. REGRA DE NEGÓCIO + JOIN 
-- Pergunta de negócio:
-- Quais ordens de serviço são de garantia e quais serviços
-- foram registrados nessas ordens, mostrando também o veículo?
-- ============================================================

SELECT
    os.id_ordem_servico AS os_garantia,
    os.id_os_garantia AS os_original,
    v.modelo AS veiculo,
    s.nome AS servico,
    oss.quantidade,
    oss.valor_cobrado
FROM ordem_servico os
INNER JOIN veiculo v
    ON v.id_veiculo = os.id_veiculo
INNER JOIN os_servico oss
    ON oss.id_ordem_servico = os.id_ordem_servico
INNER JOIN servico s
    ON s.id_servico = oss.id_servico
WHERE os.id_os_garantia IS NOT NULL
ORDER BY os.id_ordem_servico, s.id_servico;
