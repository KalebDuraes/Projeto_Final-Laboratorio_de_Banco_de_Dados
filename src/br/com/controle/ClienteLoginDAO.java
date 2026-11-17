/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.controle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteLoginDAO {

    public boolean validarLogin(String documento, String senha) throws SQLException {
        String sql = "SELECT id_cliente FROM cliente "
                   + "WHERE (cpf = ? OR cnpj = ?) AND senha = ?";

        // Remove caracteres não numéricos de CPF/CNPJ
        documento = documento.replaceAll("\\D", "");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documento);
            stmt.setString(2, documento);
            stmt.setString(3, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}