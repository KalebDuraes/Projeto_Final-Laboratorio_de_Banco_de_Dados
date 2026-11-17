/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.controle;

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
            
            stmt.setString(1, placa.trim().toUpperCase());
            stmt.setString(2, modelo.trim());
            stmt.setString(3, marca.trim());
            stmt.setInt(4, ano);
            stmt.setString(5, cor.trim());
            stmt.setInt(6, idCliente);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Erro: Já existe um veículo cadastrado com esta placa.", e);
            } else if (e.getErrorCode() == 1452) {
                throw new SQLException("Erro: Não existe cliente cadastrado com o ID " + idCliente + ".", e);
            }
            throw e;
        }
    }

    // ==========================================
    // READ (Buscar Veículo por Placa - Desacoplado do Swing)
    // Retorna String[5]: [0]=modelo, [1]=marca, [2]=ano, [3]=cor, [4]=id_cliente
    // ==========================================
    public String[] buscarPorPlaca(String placa) throws SQLException {
        String sql = "SELECT modelo, marca, ano, cor, id_cliente FROM veiculo WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, placa.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("modelo"),
                        rs.getString("marca"),
                        String.valueOf(rs.getInt("ano")),
                        rs.getString("cor"),
                        String.valueOf(rs.getInt("id_cliente"))
                    };
                }
            }
        }
        return null;
    }

    // ==========================================
    // UPDATE (Atualizar Veículo por Placa)
    // ==========================================
    public void atualizarPorPlaca(String placa, String modelo, String marca, int ano, String cor, int idCliente) throws SQLException {
        String sql = "UPDATE veiculo SET modelo = ?, marca = ?, ano = ?, cor = ?, id_cliente = ? WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, modelo.trim());
            stmt.setString(2, marca.trim());
            stmt.setInt(3, ano);
            stmt.setString(4, cor.trim());
            stmt.setInt(5, idCliente);
            stmt.setString(6, placa.trim().toUpperCase());
            
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum veículo encontrado com a placa informada para atualização.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1452) {
                throw new SQLException("Erro: Não existe cliente cadastrado com o ID " + idCliente + ".", e);
            }
            throw e;
        }
    }

    // ==========================================
    // DELETE (Remover Veículo por Placa)
    // ==========================================
    public void deletarPorPlaca(String placa) throws SQLException {
        String sql = "DELETE FROM veiculo WHERE placa = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, placa.trim().toUpperCase());
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum veículo encontrado com a placa informada para exclusão.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este veículo pois ele possui ordens de serviço vinculadas.", e);
            }
            throw e;
        }
    }
}