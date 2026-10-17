# Projeto_Final-Laboratorio_de_Banco_de_Dados
Trabalho do Grupo 6, composto por: 
Ana Luiza Vieira Peixoto,
Arthur Moreira de Alencar,
João Pedro Gomes de Moraes,
Kaleb Kelvin da Costa Durães,
Rianne Moreira Siqueira.

O tema escolhido para o projeto foi um 'Sistema de Gerenciamento de Oficina Mecânica'

1. Descrição do projeto
O projeto consiste no desenvolvimento de um banco de dados relacional para um sistema de gerenciamento de oficina mecânica.
O sistema permite organizar informações de clientes, veículos, atendentes, mecânicos, serviços, peças, ordens de serviço, pagamentos e histórico de atendimento.

2. Tecnologias utilizadas
SQL
MySQL
MySQL Workbench
Java (estrutura do projeto disponibilizada no repositório)

3. Estrutura do banco de dados
O banco de dados utilizado no projeto chama-se oficina_mecanica.

Entre as principais entidades estão:
Cliente e seus contatos.
Veículo.
Atendente e mecânico.
Catálogo de serviços e peças.
Ordem de serviço e seu histórico.
Pagamento.
Associações entre ordens de serviço, serviços, peças e mecânicos.

4. Organização dos arquivos SQL:
- 01_ddl.sql: criação do banco de dados, tabelas, restrições e índices.
- 02_carga.sql: inserção dos dados de teste.
- 03_consultas.sql: consultas SQL para responder às perguntas de negócio.
- 04_otimizacao.sql: consolidação dos três testes de análise de desempenho.
- 04.1_testes.sql: testes das consultas de otimização.
  
5. Como executar o projeto:
Abra o MySQL Workbench e conecte-se ao servidor MySQL.
Execute o script 01_ddlsql.sql para criar a estrutura do banco.
Execute 02_cargasol.sql para carregar os dados de teste.
Execute 03_consultas.sql para testar as consultas.
Execute 04_otimizacao.sql para analisar os planos de execução com EXPLAIN ANALYZE.

(Atenção: o script de DDL pode conter comandos que apagam e recriam o banco. Execute-o somente quando estiver preparado para substituir os dados existentes.)
