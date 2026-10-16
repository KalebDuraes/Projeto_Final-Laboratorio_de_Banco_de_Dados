/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro

import java.sql.*;

public class OrdemServicoDAO {

    /**
     * OPERAÇÃO TRANSACIONAL PRINCIPAL (Atende B1 e B3):
     * Altera a Ordem de Serviço e insere o Pagamento de forma atômica.
     * Se ocorrer qualquer falha, o ROLLBACK desfaz as operações no banco.
     */
    public void concluirOSComPagamento(int idOrdemServico, double valorTotal, String formaPagamento) throws SQLException {
        String sqlAtualizaOS = "UPDATE ordem_servico SET situacao = 'CONCLUIDA', data_fechamento = CURDATE() WHERE id_ordem_servico = ?";
        String sqlInserePagamento = "INSERT INTO pagamento (id_ordem_servico, valor_total, forma_pagamento, data_pagamento) VALUES (?, ?, ?, NOW())";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            
            // Início da Transação: Desliga o auto-commit
            conn.setAutoCommit(false);

            // 1. Atualiza o status da OS
            try (PreparedStatement stmtOS = conn.prepareStatement(sqlAtualizaOS)) {
                stmtOS.setInt(1, idOrdemServico);
                stmtOS.executeUpdate();
            }

            // 2. Registra o pagamento vinculado
            try (PreparedStatement stmtPag = conn.prepareStatement(sqlInserePagamento)) {
                stmtPag.setInt(1, idOrdemServico);
                stmtPag.setDouble(2, valorTotal);
                stmtPag.setString(3, formaPagamento);
                stmtPag.executeUpdate();
            }

            // Sucesso: Efetiva as alterações
            conn.commit();

        } catch (SQLException e) {
            // Falha: Desfaz as alterações
            if (conn != null) {
                try {
                    conn.rollback();
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