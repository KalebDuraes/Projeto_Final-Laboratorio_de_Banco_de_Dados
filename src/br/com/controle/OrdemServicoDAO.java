package br.com.controle;

// @author João Pedro
import java.sql.*;

public class OrdemServicoDAO {

    // ==========================================
    // CREATE (Inserir Ordem de Serviço + Mecânico em os_mecanico via Transação)
    // ==========================================
    public int inserir(int idVeiculo, int idAtendente, int idMecanico, String status, Integer idOsGarantia) throws SQLException {
        String sqlOs = "INSERT INTO ordem_servico (data_abertura, id_veiculo, id_atendente, situacao, id_os_garantia) " +
                       "VALUES (CURDATE(), ?, ?, ?, ?)";
        String sqlMec = "INSERT INTO os_mecanico (id_ordem_servico, id_mecanico) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            int idGerado;
            try (PreparedStatement stmtOs = conn.prepareStatement(sqlOs, Statement.RETURN_GENERATED_KEYS)) {
                stmtOs.setInt(1, idVeiculo);
                stmtOs.setInt(2, idAtendente);
                stmtOs.setString(3, (status == null || status.trim().isEmpty()) ? "ABERTA" : status.trim().toUpperCase());

                if (idOsGarantia != null) {
                    stmtOs.setInt(4, idOsGarantia);
                } else {
                    stmtOs.setNull(4, Types.INTEGER);
                }

                stmtOs.executeUpdate();

                try (ResultSet rsKeys = stmtOs.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idGerado = rsKeys.getInt(1);
                    } else {
                        throw new SQLException("Falha ao obter o ID gerado da Ordem de Serviço.");
                    }
                }
            }

            try (PreparedStatement stmtMec = conn.prepareStatement(sqlMec)) {
                stmtMec.setInt(1, idGerado);
                stmtMec.setInt(2, idMecanico);
                stmtMec.executeUpdate();
            }

            conn.commit();
            return idGerado;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getErrorCode() == 1452) {
                throw new SQLException("Erro de chave estrangeira: Verifique se os IDs de Veículo, Atendente, Mecânico ou OS de Garantia existem no banco.", e);
            } else if (e.getErrorCode() == 3819 || e.getErrorCode() == 1265) {
                throw new SQLException("Erro: Situação inválida. Use: ABERTA, EM_EXECUCAO, AGUARDANDO_PECAS, CONCLUIDA ou CANCELADA.", e);
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // ==========================================
    // READ (Buscar OS por ID - Desacoplado do Swing)
    // Retorna String[5]: [0]=id_veiculo, [1]=id_atendente, [2]=id_mecanico, [3]=situacao, [4]=id_os_garantia
    // ==========================================
    public String[] buscarPorId(int idOs) throws SQLException {
        String sql = "SELECT os.id_veiculo, os.id_atendente, os.situacao, os.id_os_garantia, om.id_mecanico " +
                     "FROM ordem_servico os " +
                     "LEFT JOIN os_mecanico om ON os.id_ordem_servico = om.id_ordem_servico " +
                     "WHERE os.id_ordem_servico = ? LIMIT 1";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOs);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idMec = rs.getInt("id_mecanico");
                    int idGarantia = rs.getInt("id_os_garantia");
                    return new String[]{
                        String.valueOf(rs.getInt("id_veiculo")),
                        String.valueOf(rs.getInt("id_atendente")),
                        idMec > 0 ? String.valueOf(idMec) : "",
                        rs.getString("situacao"),
                        idGarantia > 0 ? String.valueOf(idGarantia) : ""
                    };
                }
            }
        }
        return null;
    }

    // ==========================================
    // UPDATE (Atualizar OS por ID + Mecânico Responsável)
    // ==========================================
    public void atualizar(int idOs, int idVeiculo, int idAtendente, int idMecanico, String status, Integer idOsGarantia) throws SQLException {
        String situacaoFormatada = status.trim().toUpperCase();
        boolean encerrada = "CONCLUIDA".equals(situacaoFormatada) || "CANCELADA".equals(situacaoFormatada);

        String sqlOs = "UPDATE ordem_servico SET id_veiculo = ?, id_atendente = ?, situacao = ?, id_os_garantia = ?, " +
                       "data_fechamento = " + (encerrada ? "CURDATE()" : "NULL") + " " +
                       "WHERE id_ordem_servico = ?";
        String sqlDelMec = "DELETE FROM os_mecanico WHERE id_ordem_servico = ?";
        String sqlInsMec = "INSERT INTO os_mecanico (id_ordem_servico, id_mecanico, papel) VALUES (?, ?, 'RESPONSAVEL')";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOs = conn.prepareStatement(sqlOs)) {
                stmtOs.setInt(1, idVeiculo);
                stmtOs.setInt(2, idAtendente);
                stmtOs.setString(3, situacaoFormatada);

                if (idOsGarantia != null) {
                    stmtOs.setInt(4, idOsGarantia);
                } else {
                    stmtOs.setNull(4, Types.INTEGER);
                }
                stmtOs.setInt(5, idOs);

                int linhas = stmtOs.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Nenhuma Ordem de Serviço encontrada com o ID " + idOs + ".");
                }
            }

            try (PreparedStatement stmtDel = conn.prepareStatement(sqlDelMec)) {
                stmtDel.setInt(1, idOs);
                stmtDel.executeUpdate();
            }

            try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsMec)) {
                stmtIns.setInt(1, idOs);
                stmtIns.setInt(2, idMecanico);
                stmtIns.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getErrorCode() == 1452) {
                throw new SQLException("Erro: Um dos IDs informados (Veículo, Atendente, Mecânico ou Garantia) não existe.", e);
            } else if (e.getErrorCode() == 3819 || e.getErrorCode() == 1265) {
                throw new SQLException("Erro: Situação inválida ou data de fechamento inconsistente.", e);
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // ==========================================
    // DELETE (Excluir OS por ID + Vínculo em os_mecanico)
    // ==========================================
    public void deletar(int idOs) throws SQLException {
        String sqlMec = "DELETE FROM os_mecanico WHERE id_ordem_servico = ?";
        String sqlOs = "DELETE FROM ordem_servico WHERE id_ordem_servico = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtMec = conn.prepareStatement(sqlMec)) {
                stmtMec.setInt(1, idOs);
                stmtMec.executeUpdate();
            }

            try (PreparedStatement stmtOs = conn.prepareStatement(sqlOs)) {
                stmtOs.setInt(1, idOs);
                int linhas = stmtOs.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Nenhuma Ordem de Serviço encontrada com o ID " + idOs + " para exclusão.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir esta OS pois existem registros de pagamento, garantia ou itens associados.", e);
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // ==========================================
    // TRANSAÇÃO (Atende B1 e B3: Concluir OS com Pagamento)
    // ==========================================
    public void concluirOSComPagamento(int idOrdemServico, double valorTotal, String formaPagamento) throws SQLException {
        String sqlAtualizaOS = "UPDATE ordem_servico SET situacao = 'CONCLUIDA', data_fechamento = CURDATE() WHERE id_ordem_servico = ?";
        String sqlInserePagamento = "INSERT INTO pagamento (id_ordem_servico, valor_total, forma_pagamento, data_pagamento) VALUES (?, ?, ?, NOW())";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOS = conn.prepareStatement(sqlAtualizaOS)) {
                stmtOS.setInt(1, idOrdemServico);
                int linhas = stmtOS.executeUpdate();
                if (linhas == 0) {
                    throw new SQLException("Ordem de Serviço #" + idOrdemServico + " não encontrada.");
                }
            }

            try (PreparedStatement stmtPag = conn.prepareStatement(sqlInserePagamento)) {
                stmtPag.setInt(1, idOrdemServico);
                stmtPag.setDouble(2, valorTotal);
                stmtPag.setString(3, formaPagamento);
                stmtPag.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
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