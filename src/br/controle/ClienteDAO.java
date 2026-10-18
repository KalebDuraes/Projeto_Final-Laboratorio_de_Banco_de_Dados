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
    // READ (Buscar Cliente por CPF para Preencher a Tela)
    // ==========================================
    public boolean buscarPorCpf(String cpf, javax.swing.JTextField txtNome, javax.swing.JComboBox<String> comboTipo, 
            javax.swing.JTextField txtCnpj, javax.swing.JTextField txtCep, javax.swing.JTextField txtEstado, 
            javax.swing.JTextField txtCidade, javax.swing.JTextField txtBairro, javax.swing.JTextField txtLogradouro, 
            javax.swing.JTextField txtNumero, javax.swing.JTextField txtComplemento) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE cpf = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtNome.setText(rs.getString("nome"));
                    String tipo = rs.getString("tipo_pessoa");
                    if (tipo != null && (tipo.equalsIgnoreCase("F") || tipo.contains("Física"))) {
                        comboTipo.setSelectedIndex(0);
                    } else {
                        comboTipo.setSelectedIndex(1);
                    }
                    txtCnpj.setText(rs.getString("cnpj"));
                    txtCep.setText(rs.getString("cep"));
                    txtEstado.setText(rs.getString("estado"));
                    txtCidade.setText(rs.getString("cidade"));
                    txtBairro.setText(rs.getString("bairro"));
                    txtLogradouro.setText(rs.getString("logradouro"));
                    txtNumero.setText(rs.getString("numero"));
                    txtComplemento.setText(rs.getString("complemento"));
                    return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // UPDATE (Atualizar Cliente por CPF)
    // ==========================================
    public void atualizarPorCpf(String nome, String cep, String estado, String cidade, String bairro, String logradouro, String numero, String complemento, char tipoPessoa, String cpf, String cnpj) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, cep = ?, estado = ?, cidade = ?, bairro = ?, logradouro = ?, numero = ?, complemento = ?, tipo_pessoa = ?, cnpj = ? WHERE cpf = ?";
        
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
            stmt.setString(10, cnpj);
            stmt.setString(11, cpf);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // DELETE (Remover Cliente por CPF)
    // ==========================================
    public void deletarPorCpf(String cpf) throws SQLException {
        String sql = "DELETE FROM cliente WHERE cpf = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este cliente pois ele possui veículos ou registros vinculados no sistema.", e);
            }
            throw e;
        }
    }
}