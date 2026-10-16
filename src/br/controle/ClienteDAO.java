/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ==========================================
    // CREATE (Inserir Cliente)
    // ==========================================
    public void inserir(String nome, String cep, String estado, String cidade, String bairro, String logradouro, String numero, String complemento, char tipoPessoa, String cpf, String cnpj) throws SQLException {
        String sql = "INSERT INTO cliente (nome, cep, estado, cidade, bairro, logradouro, numero, complemento, tipo_pessoa, cpf, cnpj) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.setString(2, cep);
            stmt.setString(3, estado);
            stmt.setString(4, cidade);
            stmt.setString(5, bairro);
            stmt.setString(6, logradouro);
            stmt.setString(7, numero);
            stmt.setString(8, complemento);
            stmt.setString(9, String.valueOf(tipoPessoa));
            stmt.setString(10, cpf);
            stmt.setString(11, cnpj);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Erro: Já existe um cliente cadastrado com este CPF ou CNPJ.", e);
            }
            throw e;
        }
    }

    // ==========================================
    // READ (Listar Clientes para a Tabela da Interface)
    // ==========================================
    public List<Object[]> listar() throws SQLException {
        String sql = "SELECT id_cliente, nome, cpf, cnpj, tipo_pessoa FROM cliente";
        List<Object[]> clientes = new ArrayList<>();
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] linha = new Object[5];
                linha[0] = rs.getInt("id_cliente");
                linha[1] = rs.getString("nome");
                linha[2] = rs.getString("cpf") != null ? rs.getString("cpf") : rs.getString("cnpj");
                linha[3] = rs.getString("tipo_pessoa");
                clientes.add(linha);
            }
        }
        return clientes;
    }

    // ==========================================
    // UPDATE (Atualizar Cliente)
    // ==========================================
    public void atualizar(int idCliente, String nome, String cep, String estado, String cidade, String bairro, String logradouro, String numero, String complemento) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, cep = ?, estado = ?, cidade = ?, bairro = ?, logradouro = ?, numero = ?, complemento = ? WHERE id_cliente = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.setString(2, cep);
            stmt.setString(3, estado);
            stmt.setString(4, cidade);
            stmt.setString(5, bairro);
            stmt.setString(6, logradouro);
            stmt.setString(7, numero);
            stmt.setString(8, complemento);
            stmt.setInt(9, idCliente);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // DELETE (Remover Cliente)
    // ==========================================
    public void deletar(int idCliente) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCliente);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este cliente pois ele possui veículos ou registros vinculados no sistema.", e);
            }
            throw e;
        }
    }
}