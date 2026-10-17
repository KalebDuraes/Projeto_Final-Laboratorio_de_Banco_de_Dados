-- ============================================================
-- PROJETO FINAL: OFICINA MECANICA
-- ETAPA 2 - B4: OTIMIZACAO DE CONSULTAS
-- Banco: oficina_mecanica
-- SGBD: MySQL 8.0.18 ou superior
--
-- Objetivo:
-- Avaliar tres consultas, documentar os indices utilizados
-- e permitir a analise dos planos com EXPLAIN ANALYZE.
--
-- IMPORTANTE:
-- Os indices experimentais ja existem no banco.
-- Os comandos CREATE INDEX abaixo estao comentados apenas
-- para documentar a intervencao realizada.
-- NAO DESCOMENTAR NEM EXECUTAR NOVAMENTE sem verificar
-- previamente os indices existentes.
--
-- Os testes controlados estao no arquivo 04.1_testes.sql.
-- ============================================================

USE oficina_mecanica;


-- ============================================================
-- INDICES EXPERIMENTAIS - DOCUMENTACAO DA INTERVENCAO
-- ============================================================

-- INDICE DA CONSULTA 1
-- CREATE INDEX idx_os_situacao_fechamento_abertura
-- ON ordem_servico (situacao, data_fechamento, data_abertura);
--
-- Justificativa:
-- situacao: filtro por uma lista de situacoes.
-- data_fechamento: filtro IS NULL.
-- data_abertura: filtro por intervalo de datas.
-- A ordem considera primeiro os filtros de igualdade/IS NULL
-- e depois o intervalo de datas.


-- INDICE DA CONSULTA 2
-- CREATE INDEX idx_oss_servico_ordem
-- ON os_servico (id_servico, id_ordem_servico);
--
-- Justificativa:
-- id_servico vem primeiro porque e utilizado na juncao
-- com servico e no agrupamento por servico.
-- id_ordem_servico complementa o indice e pode auxiliar
-- a contagem de ordens distintas por servico.


-- INDICE DA CONSULTA 3
-- CREATE INDEX idx_pagamento_forma_data
-- ON pagamento (forma_pagamento, data_pagamento);
--
-- Justificativa:
-- forma_pagamento vem primeiro porque e filtrada por igualdade.
-- data_pagamento vem depois porque e filtrada por intervalo.
-- Essa ordem permite combinar os dois criterios de busca.


-- ============================================================
-- CONSULTA 1: ORDENS DE SERVICO EM ABERTO
-- ============================================================
-- Objetivo:
-- Localizar ordens nao encerradas, sem data de fechamento,
-- abertas entre 17/09/2026 e 20/09/2026, inclusive.
--
-- Indices relevantes:
-- idx_os_data_abertura (data_abertura)
-- idx_os_situacao_fechamento_abertura
-- (situacao, data_fechamento, data_abertura)
--
-- Interpretacao esperada:
-- Verificar o indice escolhido, as linhas examinadas,
-- as linhas retornadas e os tempos das operacoes do plano.

EXPLAIN ANALYZE
SELECT
    id_ordem_servico,
    data_abertura,
    situacao,
    id_veiculo
FROM ordem_servico
WHERE situacao IN (
    'ABERTA',
    'EM_EXECUCAO',
    'AGUARDANDO_PECAS'
)
AND data_fechamento IS NULL
AND data_abertura >= '2026-09-17'
AND data_abertura < '2026-09-21';


-- ============================================================
-- CONSULTA 2: SERVICOS MAIS EXECUTADOS
-- ============================================================
-- Objetivo:
-- Identificar servicos presentes em mais de cinco ordens
-- de servico distintas.
--
-- Indice experimental:
-- idx_oss_servico_ordem (id_servico, id_ordem_servico)
--
-- Operacoes analisadas:
-- JOIN, COUNT DISTINCT, GROUP BY, HAVING e ORDER BY.
--
-- Interpretacao esperada:
-- Verificar como ocorre a juncao, quantas linhas sao
-- processadas, como os grupos sao agregados e ordenados.

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
-- CONSULTA 3: PAGAMENTOS POR PIX
-- ============================================================
-- Objetivo:
-- Contar os pagamentos por PIX e somar os valores recebidos
-- durante o ano de 2026.
--
-- Indice experimental:
-- idx_pagamento_forma_data (forma_pagamento, data_pagamento)
--
-- Interpretacao esperada:
-- Verificar o acesso aos registros, a filtragem por forma
-- e periodo, e a operacao de agregacao.

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
-- FIM DO ARQUIVO DE OTIMIZACAO
--
-- Os resultados dos planos devem ser registrados a partir das execucoes reais.
-- Para comparar cenarios, utilizar 04.1_testes.sql.
-- ============================================================
