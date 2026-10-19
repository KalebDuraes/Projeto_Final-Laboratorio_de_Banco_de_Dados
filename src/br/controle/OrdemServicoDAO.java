/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro

import java.sql.*;

public class OrdemServicoDAO {

    // ==========================================
    // CREATE (Inserir Ordem de Serviço)
    // ==========================================
    public void inserir(int idVeiculo, int idAtendente, int idMecanico, String status, Integer idOsGarantia) throws SQLException {
        String sql = "INSERT INTO ordem_servico (id_veiculo, id_atendente, id_mecanico, situacao, id_os_garantia) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVeiculo);
            stmt.setInt(2, idAtendente);
            stmt.setInt(3, idMecanico);
            stmt.setString(4, status);
            if (idOsGarantia != null) {
                stmt.setInt(5, idOsGarantia);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // READ (Buscar OS por ID)
    // ==========================================
    public boolean buscarPorId(int idOs, javax.swing.JTextField txtVeiculo, javax.swing.JTextField txtAtendente, 
            javax.swing.JTextField txtMecanico, javax.swing.JTextField txtStatus, javax.swing.JTextField txtGarantia) throws SQLException {
        String sql = "SELECT * FROM ordem_servico WHERE id_ordem_servico = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idOs);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtVeiculo.setText(String.valueOf(rs.getInt("id_veiculo")));
                    txtAtendente.setText(String.valueOf(rs.getInt("id_atendente")));
                    txtMecanico.setText(String.valueOf(rs.getInt("id_mecanico")));
                    txtStatus.setText(rs.getString("situacao"));
                    int garantia = rs.getInt("id_os_garantia");
                    txtGarantia.setText(rs.wasNull() ? "" : String.valueOf(garantia));
                    return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // UPDATE (Atualizar OS por ID)
    // ==========================================
    public void atualizar(int idOs, int idVeiculo, int idAtendente, int idMecanico, String status, Integer idOsGarantia) throws SQLException {
        String sql = "UPDATE ordem_servico SET id_veiculo = ?, id_atendente = ?, id_mecanico = ?, situacao = ?, id_os_garantia = ? WHERE id_ordem_servico = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVeiculo);
            stmt.setInt(2, idAtendente);
            stmt.setInt(3, idMecanico);
            stmt.setString(4, status);
            if (idOsGarantia != null) {
                stmt.setInt(5, idOsGarantia);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, idOs);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // DELETE (Excluir OS por ID)
    // ==========================================
    public void deletar(int idOs) throws SQLException {
        String sql = "DELETE FROM ordem_servico WHERE id_ordem_servico = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idOs);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir esta OS pois existem registos de pagamento ou itens associados.", e);
            }
            throw e;
        }
    }

    // ==========================================
    // TRANSAÇÃO (Atende B1 e B3: Concluir OS com Pagamento)[cite: 6, 11, 20]
    // ==========================================
    public void concluirOSComPagamento(int idOrdemServico, double valorTotal, String formaPagamento) throws SQLException {
        String sqlAtualizaOS = "UPDATE ordem_servico SET situacao = 'CONCLUIDA', data_fechamento = CURDATE() WHERE id_ordem_servico = ?";
        String sqlInserePagamento = "INSERT INTO pagamento (id_ordem_servico, valor_total, forma_pagamento, data_pagamento) VALUES (?, ?, ?, NOW())";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // Início da Transação[cite: 6, 20]

            try (PreparedStatement stmtOS = conn.prepareStatement(sqlAtualizaOS)) {
                stmtOS.setInt(1, idOrdemServico);
                stmtOS.executeUpdate();
            }

            try (PreparedStatement stmtPag = conn.prepareStatement(sqlInserePagamento)) {
                stmtPag.setInt(1, idOrdemServico);
                stmtPag.setDouble(2, valorTotal);
                stmtPag.setString(3, formaPagamento);
                stmtPag.executeUpdate();
            }

            conn.commit(); // Sucesso: Efetiva as alterações[cite: 6, 20]

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Falha: Desfaz as alterações[cite: 6, 20]
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Erro ao concluir a OS e registrar o pagamento (Transação revertida): " + e.getMessage(), e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}