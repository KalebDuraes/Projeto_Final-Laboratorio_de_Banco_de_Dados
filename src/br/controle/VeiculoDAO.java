/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro
import java.sql.*;

public class VeiculoDAO {

    // ==========================================
    // CREATE (Inserir Veículo)
    // ==========================================
    public void inserir(String placa, String modelo, String marca, int ano, String cor, int idCliente) throws SQLException {
        String sql = "INSERT INTO veiculo (placa, modelo, marca, ano, cor, id_cliente) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, placa);
            stmt.setString(2, modelo);
            stmt.setString(3, marca);
            stmt.setInt(4, ano);
            stmt.setString(5, cor);
            stmt.setInt(6, idCliente);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Erro: Já existe um veículo cadastrado com esta placa.", e);
            }
            throw e;
        }
    }

    // ==========================================
    // READ (Buscar Veículo por Placa)
    // ==========================================
    public boolean buscarPorPlaca(String placa, javax.swing.JTextField txtModelo, javax.swing.JTextField txtMarca, 
            javax.swing.JTextField txtAno, javax.swing.JTextField txtCor, javax.swing.JTextField txtCliente) throws SQLException {
        String sql = "SELECT * FROM veiculo WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, placa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtModelo.setText(rs.getString("modelo"));
                    txtMarca.setText(rs.getString("marca"));
                    txtAno.setText(String.valueOf(rs.getInt("ano")));
                    txtCor.setText(rs.getString("cor"));
                    txtCliente.setText(String.valueOf(rs.getInt("id_cliente")));
                    return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // UPDATE (Atualizar Veículo por Placa)
    // ==========================================
    public void atualizarPorPlaca(String placa, String modelo, String marca, int ano, String cor, int idCliente) throws SQLException {
        String sql = "UPDATE veiculo SET modelo = ?, marca = ?, ano = ?, cor = ?, id_cliente = ? WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, modelo);
            stmt.setString(2, marca);
            stmt.setInt(3, ano);
            stmt.setString(4, cor);
            stmt.setInt(5, idCliente);
            stmt.setString(6, placa);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // DELETE (Remover Veículo por Placa)
    // ==========================================
    public void deletarPorPlaca(String placa) throws SQLException {
        String sql = "DELETE FROM veiculo WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, placa);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este veículo pois ele possui ordens de serviço vinculadas.", e);
            }
            throw e;
        }
    }
}