/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.controle;

// @author João Pedro

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    // URL de conexão com o banco ajustada para UTF-8 e fuso horário correto
    private static final String URL = "jdbc:mysql://localhost:3306/oficina_mecanica?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Ajustar conforme o ambiente do grupo
    private static final String PASSWORD = ""; // Ajustar conforme o ambiente do grupo

    public static Connection conectar() throws SQLException {
        try {
            // Garante o carregamento do driver JDBC do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do MySQL não encontrado no classpath do projeto!", e);
        }
    }
}