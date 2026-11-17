package br.com.controle;

// @author João Pedro e Kaleb
import java.sql.*;

public class PecaServicoDAO {

    // ==========================================
    // CREATE (Inserir Serviço ou Peça)
    // ==========================================
    public void inserir(boolean isServico, String nome, String descricao, double valor) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new SQLException("O nome do item é obrigatório.");
        }

        String sql = isServico
                ? "INSERT INTO servico (nome, descricao, valor_referencia) VALUES (?, ?, ?)"
                : "INSERT INTO peca (nome, descricao, valor_unitario) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome.trim());

            if (descricao == null || descricao.trim().isEmpty() || descricao.equals("Descrição do item")) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, descricao.trim());
            }

            stmt.setDouble(3, valor);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 3819) {
                throw new SQLException("Erro: O valor do item não pode ser negativo.", e);
            }
            throw e;
        }
    }

    // ==========================================
    // READ (Buscar por Nome - Desacoplado do Swing)
    // Retorna String[2]: [0]=descricao, [1]=valor
    // ==========================================
    public String[] buscarPorNome(boolean isServico, String nome) throws SQLException {
        String tabela = isServico ? "servico" : "peca";
        String colunaValor = isServico ? "valor_referencia" : "valor_unitario";
        String sql = "SELECT descricao, " + colunaValor + " FROM " + tabela + " WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String desc = rs.getString("descricao");
                    return new String[]{
                        desc != null ? desc : "",
                        String.valueOf(rs.getDouble(colunaValor))
                    };
                }
            }
        }
        return null;
    }

    // ==========================================
    // UPDATE (Atualizar por Nome)
    // ==========================================
    public void atualizarPorNome(boolean isServico, String nome, String descricao, double valor) throws SQLException {
        String tabela = isServico ? "servico" : "peca";
        String colunaValor = isServico ? "valor_referencia" : "valor_unitario";
        String sql = "UPDATE " + tabela + " SET descricao = ?, " + colunaValor + " = ? WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (descricao == null || descricao.trim().isEmpty()) {
                stmt.setNull(1, Types.VARCHAR);
            } else {
                stmt.setString(1, descricao.trim());
            }

            stmt.setDouble(2, valor);
            stmt.setString(3, nome.trim());

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum item encontrado com o nome '" + nome + "' para atualizar.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 3819) {
                throw new SQLException("Erro: O valor do item não pode ser negativo.", e);
            }
            throw e;
        }
    }

    // ==========================================
    // DELETE (Remover por Nome)
    // ==========================================
    public void deletarPorNome(boolean isServico, String nome) throws SQLException {
        String tabela = isServico ? "servico" : "peca";
        String sql = "DELETE FROM " + tabela + " WHERE nome = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome.trim());
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum item encontrado com o nome '" + nome + "' para exclusão.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este item pois ele já está vinculado a uma Ordem de Serviço.", e);
            }
            throw e;
        }
    }
}