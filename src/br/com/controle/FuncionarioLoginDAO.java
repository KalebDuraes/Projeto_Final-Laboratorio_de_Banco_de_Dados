/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.controle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionarioLoginDAO {

    public boolean validarLogin(String cpf, String senha) throws SQLException {
        String sql = "SELECT 1 FROM atendente "
                   + "WHERE cpf = ? AND senha = ? "
                   + "UNION ALL "
                   + "SELECT 1 FROM mecanico "
                   + "WHERE cpf = ? AND senha = ? "
                   + "LIMIT 1";

        cpf = cpf.replaceAll("\\D", "");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            stmt.setString(2, senha);
            stmt.setString(3, cpf);
            stmt.setString(4, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
