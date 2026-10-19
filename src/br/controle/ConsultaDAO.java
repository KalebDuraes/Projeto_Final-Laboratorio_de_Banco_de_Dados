/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro

import java.sql.*;

public class ConsultaDAO {

    // 1. Consulta: Ordens de Serviço (Junção com Veículo e Cliente)
    public String consultarOrdensServico() throws SQLException {
        String sql = "SELECT os.id_ordem_servico, os.situacao, c.nome, v.modelo, v.placa " +
                     "FROM ordem_servico os " +
                     "JOIN veiculo v ON os.id_veiculo = v.id_veiculo " +
                     "JOIN cliente c ON v.id_cliente = c.id_cliente";
        
        StringBuilder resultado = new StringBuilder("=== RELATÓRIO DE ORDENS DE SERVIÇO ===\n\n");
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                resultado.append("ID OS: ").append(rs.getInt("id_ordem_servico"))
                         .append(" | Situação: ").append(rs.getString("situacao"))
                         .append(" | Cliente: ").append(rs.getString("nome"))
                         .append(" | Veículo: ").append(rs.getString("modelo"))
                         .append(" (Placa: ").append(rs.getString("placa")).append(")\n");
            }
        }
        return resultado.length() > 40 ? resultado.toString() : "Nenhuma ordem de serviço encontrada.";
    }

    // 2. Consulta: Veículos e Clientes (Agregação / Relatório detalhado)
    public String consultarVeiculosClientes() throws SQLException {
        String sql = "SELECT c.nome, c.cpf, COUNT(v.id_veiculo) as total_veiculos " +
                     "FROM cliente c LEFT JOIN veiculo v ON c.id_cliente = v.id_cliente " +
                     "GROUP BY c.id_cliente, c.nome, c.cpf";
        
        StringBuilder resultado = new StringBuilder("=== RELATÓRIO DE VEÍCULOS POR CLIENTE ===\n\n");
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                resultado.append("Cliente: ").append(rs.getString("nome"))
                         .append(" | CPF: ").append(rs.getString("cpf"))
                         .append(" | Total de Veículos: ").append(rs.getInt("total_veiculos"))
                         .append("\n");
            }
        }
        return resultado.toString();
    }

    // 3. Relatório Consolidado (Alimentado por visão ou dados financeiros)
    public String consultarRelatorioConsolidado() throws SQLException {
        String sql = "SELECT * FROM vw_relatorio_consolidado"; // Substituir pelo nome da view criada no banco se necessário
        StringBuilder resultado = new StringBuilder("=== RELATÓRIO CONSOLIDADO DO SISTEMA ===\n\n");
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int colunas = metaData.getColumnCount();
            
            while (rs.next()) {
                for (int i = 1; i <= colunas; i++) {
                    resultado.append(metaData.getColumnName(i)).append(": ").append(rs.getString(i)).append(" | ");
                }
                resultado.append("\n");
            }
        } catch (SQLException e) {
            // Fallback caso a view específica ainda não esteja criada, exibindo pagamentos
            return consultarPagamentosFallback();
        }
        return resultado.toString();
    }
    
    private String consultarPagamentosFallback() throws SQLException {
        String sql = "SELECT id_pagamento, valor_total, forma_pagamento, data_pagamento FROM pagamento";
        StringBuilder resultado = new StringBuilder("=== RELATÓRIO DE PAGAMENTOS (CONSOLIDADO) ===\n\n");
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            double totalGeral = 0;
            while (rs.next()) {
                double valor = rs.getDouble("valor_total");
                totalGeral += valor;
                resultado.append("ID Pagamento: ").append(rs.getInt("id_pagamento"))
                         .append(" | Valor: R$ ").append(valor)
                         .append(" | Forma: ").append(rs.getString("forma_pagamento"))
                         .append("\n");
            }
            resultado.append("\nTOTAL GERAL ARRECADADO: R$ ").append(totalGeral);
        }
        return resultado.toString();
    }
}