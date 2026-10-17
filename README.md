# Projeto_Final-Laboratorio_de_Banco_de_Dados
Trabalho do Grupo 6, composto por: 
Ana Luiza Vieira Peixoto,
Arthur Moreira de Alencar,
João Pedro Gomes de Moraes,
Kaleb Kelvin da Costa Durães,
Rianne Moreira Siqueira.

O tema escolhido para o projeto foi 'Sistema de Gerenciamento de Oficina Mecânica'

1. Descrição do projeto:
O projeto consiste no desenvolvimento de um banco de dados para um Sistema de Gerenciamento de Oficina Mecânica, com o objetivo de organizar e gerenciar as principais informações relacionadas ao funcionamento de uma oficina. O sistema contempla o cadastro de clientes, veículos, atendentes, mecânicos, serviços e peças, além do gerenciamento de ordens de serviço, pagamentos e histórico de atendimentos.

2. Tecnologias utilizadas:
SQL: linguagem utilizada para definição, manipulação e consulta dos dados.
MySQL 8.0 ou superior: sistema gerenciador de banco de dados utilizado no projeto.
MySQL Workbench: ferramenta utilizada para executar scripts SQL, consultar dados e analisar planos de execução.
Java: tecnologia relacionada à estrutura do projeto disponibilizada no repositório.

3. Estrutura do banco de dados:
O banco de dados utilizado no projeto chama-se oficina_mecanica.
Entre as principais entidades e relacionamentos implementados estão:

Clientes e seus contatos;
Veículos associados aos clientes;
Atendentes e mecânicos;
Catálogo de serviços e peças;
Ordens de serviço e seus históricos;
Pagamentos relacionados às ordens de serviço;
Associações entre ordens de serviço, serviços, peças e mecânicos.

A estrutura foi desenvolvida para organizar os dados, estabelecer relacionamentos entre as entidades e garantir a integridade das informações por meio de chaves primárias, chaves estrangeiras e restrições de integridade.

4. Organização dos arquivos SQL:
- 01_ddl.sql: criação do banco de dados, tabelas, relacionamentos, restrições e índices definidos na estrutura inicial.
- 02_carga.sql: inserção dos dados utilizados para testar o sistema.
- 03_consultas.sql: consultas SQL destinadas a responder às perguntas de negócio do projeto.
- 04_otimizacao.sql: consultas utilizadas na análise de desempenho, com EXPLAIN ANALYZE, para avaliar os planos de execução e o uso dos índices.
- 04.1_testes.sql: testes auxiliares e comparativos para verificar o volume de dados, os índices existentes e os planos de execução com e sem a utilização dos índices experimentais.
   
5. Como executar o projeto:
Abra o MySQL Workbench e conecte-se ao servidor MySQL.
Execute o arquivo 01_ddl.sql para criar a estrutura do banco de dados.
Execute o arquivo 02_carga.sql para inserir os dados de teste.
Execute o arquivo 03_consultas.sql para verificar as consultas do projeto.
Execute os blocos de análise do arquivo 04_otimizacao.sql para observar os planos de execução com EXPLAIN ANALYZE.
Se necessário, utilize o arquivo 04.1_testes.sql para realizar os testes comparativos e auxiliares da etapa de otimização.

(OBS1: Atenção: antes de executar os arquivos de criação da estrutura ou de carga, verifique seu conteúdo. Alguns comandos podem apagar e recriar o banco de dados ou alterar os dados existentes. Execute-os somente quando estiver preparado para essas alterações.)

(OBS 2:Os arquivos de otimização devem ser executados após a criação da estrutura e a inserção dos dados. Os testes comparativos devem utilizar as mesmas consultas e condições para permitir uma análise consistente dos resultados.)
