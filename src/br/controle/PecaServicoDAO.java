package br.controle;

import java.sql.*;

public class PecaServicoDAO {

    // ==========================================
    // CREATE (Cadastrar Serviço ou Peça)
    // ==========================================
    public void inserir(String tipo, String nome, String descricao, double valor) throws SQLException {
        String sql;
        if (tipo.equalsIgnoreCase("Serviço") || tipo.equalsIgnoreCase("Servico")) {
            sql = "INSERT INTO servico (nome, descricao, valor_referencia) VALUES (?, ?, ?)";
        } else {
            sql = "INSERT INTO peca (nome, descricao, preco_unitario_sugerido) VALUES (?, ?, ?)";
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.setString(2, descricao);
            stmt.setDouble(3, valor);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // READ (Consultar por Nome)
    // ==========================================
    public boolean consultarPorNome(String tipo, String nomeBusca, javax.swing.JTextField txtDescricao, javax.swing.JTextField txtValor) throws SQLException {
        String sql = (tipo.equalsIgnoreCase("Serviço") || tipo.equalsIgnoreCase("Servico"))
            ? "SELECT descricao, valor_referencia FROM servico WHERE nome = ?"
            : "SELECT descricao, preco_unitario_sugerido FROM peca WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nomeBusca);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtDescricao.setText(rs.getString("descricao"));
                    txtValor.setText(String.valueOf(rs.getDouble(2)));
                    return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // UPDATE (Alterar Serviço ou Peça)
    // ==========================================
    public void atualizar(String tipo, String nomeAntigo, String novaDescricao, double novoValor) throws SQLException {
        String sql = (tipo.equalsIgnoreCase("Serviço") || tipo.equalsIgnoreCase("Servico"))
            ? "UPDATE servico SET descricao = ?, valor_referencia = ? WHERE nome = ?"
            : "UPDATE peca SET descricao = ?, preco_unitario_sugerido = ? WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, novaDescricao);
            stmt.setDouble(2, novoValor);
            stmt.setString(3, nomeAntigo);
            
            stmt.executeUpdate();
        }
    }

    // ==========================================
    // DELETE (Excluir Serviço ou Peça)
    // ==========================================
    public void deletar(String tipo, String nome) throws SQLException {
        String sql = (tipo.equalsIgnoreCase("Serviço") || tipo.equalsIgnoreCase("Servico"))
            ? "DELETE FROM servico WHERE nome = ?"
            : "DELETE FROM peca WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este item pois ele está associado a Ordens de Serviço.", e);
            }
            throw e;
        }
    }
}
