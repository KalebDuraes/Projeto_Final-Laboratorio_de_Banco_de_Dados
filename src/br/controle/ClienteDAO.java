package br.controle;

// @author João Pedro e Kaleb

import java.sql.*;

public class ClienteDAO {

    // ==========================================
    // CREATE (Inserir Cliente PF ou PJ)
    // ==========================================
    public void inserir(String nome, String cep, String estado, String cidade, String bairro,
                        String logradouro, String numero, String complemento, char tipoPessoa,
                        String cpf, String cnpj) throws SQLException {
        String sql = "INSERT INTO cliente (nome, cep, estado, cidade, bairro, logradouro, numero, complemento, tipo_pessoa, cpf, cnpj) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome.trim());
            stmt.setString(2, cep.replaceAll("\\D", "")); // Remove traços do CEP para caber no CHAR(8)
            stmt.setString(3, estado.trim().toUpperCase());
            stmt.setString(4, cidade.trim());
            stmt.setString(5, bairro.trim());
            stmt.setString(6, logradouro.trim());
            stmt.setString(7, numero.trim());

            if (complemento == null || complemento.trim().isEmpty() || complemento.equals("Apartamento, casa...")) {
                stmt.setNull(8, Types.VARCHAR);
            } else {
                stmt.setString(8, complemento.trim());
            }

            stmt.setString(9, String.valueOf(tipoPessoa));

            if (tipoPessoa == 'F') {
                String cpfLimpo = (cpf != null) ? cpf.replaceAll("\\D", "") : "";
                if (cpfLimpo.isEmpty()) stmt.setNull(10, Types.CHAR);
                else stmt.setString(10, cpfLimpo);
                stmt.setNull(11, Types.CHAR);
            } else {
                stmt.setNull(10, Types.CHAR);
                String cnpjLimpo = (cnpj != null) ? cnpj.replaceAll("\\D", "") : "";
                if (cnpjLimpo.isEmpty()) stmt.setNull(11, Types.CHAR);
                else stmt.setString(11, cnpjLimpo);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Erro: Já existe um cliente cadastrado com este CPF ou CNPJ.", e);
            } else if (e.getErrorCode() == 3819) {
                throw new SQLException("Erro de validação: Verifique se o Tipo de Pessoa corresponde ao documento preenchido (CPF para Física ou CNPJ para Jurídica).", e);
            } else if (e.getErrorCode() == 1406) {
                throw new SQLException("Erro: Algum campo ultrapassou o tamanho máximo permitido (ex: CEP deve ter 8 dígitos, UF 2 letras, CPF 11 dígitos).", e);
            }
            throw e;
        }
    }

    // ==========================================
    // READ (Buscar por CPF ou CNPJ - Desacoplado do Swing)
    // Retorna String[11]: [0]=nome, [1]=tipo_pessoa, [2]=cpf, [3]=cnpj, [4]=cep,
    //                     [5]=estado, [6]=cidade, [7]=bairro, [8]=logradouro, [9]=numero, [10]=complemento
    // ==========================================
    public String[] buscarPorDocumento(String cpf, String cnpj) throws SQLException {
        String cpfLimpo = (cpf != null) ? cpf.replaceAll("\\D", "") : "";
        String cnpjLimpo = (cnpj != null) ? cnpj.replaceAll("\\D", "") : "";

        boolean buscaPorCpf = !cpfLimpo.isEmpty();
        String sql = buscaPorCpf
                ? "SELECT * FROM cliente WHERE cpf = ?"
                : "SELECT * FROM cliente WHERE cnpj = ?";

        if (!buscaPorCpf && cnpjLimpo.isEmpty()) {
            throw new SQLException("Informe um CPF ou CNPJ para realizar a consulta.");
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buscaPorCpf ? cpfLimpo : cnpjLimpo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("nome"),
                        rs.getString("tipo_pessoa"),
                        rs.getString("cpf") != null ? rs.getString("cpf") : "",
                        rs.getString("cnpj") != null ? rs.getString("cnpj") : "",
                        rs.getString("cep"),
                        rs.getString("estado"),
                        rs.getString("cidade"),
                        rs.getString("bairro"),
                        rs.getString("logradouro"),
                        rs.getString("numero"),
                        rs.getString("complemento") != null ? rs.getString("complemento") : ""
                    };
                }
            }
        }
        return null;
    }

    // ==========================================
    // UPDATE (Atualizar por CPF ou CNPJ)
    // ==========================================
    public void atualizarPorDocumento(String nome, String cep, String estado, String cidade, String bairro,
                                      String logradouro, String numero, String complemento, char tipoPessoa,
                                      String cpf, String cnpj) throws SQLException {
        String cpfLimpo = (cpf != null) ? cpf.replaceAll("\\D", "") : "";
        String cnpjLimpo = (cnpj != null) ? cnpj.replaceAll("\\D", "") : "";

        boolean porCpf = (tipoPessoa == 'F');
        String sql = porCpf
                ? "UPDATE cliente SET nome = ?, cep = ?, estado = ?, cidade = ?, bairro = ?, logradouro = ?, numero = ?, complemento = ? WHERE cpf = ?"
                : "UPDATE cliente SET nome = ?, cep = ?, estado = ?, cidade = ?, bairro = ?, logradouro = ?, numero = ?, complemento = ? WHERE cnpj = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome.trim());
            stmt.setString(2, cep.replaceAll("\\D", ""));
            stmt.setString(3, estado.trim().toUpperCase());
            stmt.setString(4, cidade.trim());
            stmt.setString(5, bairro.trim());
            stmt.setString(6, logradouro.trim());
            stmt.setString(7, numero.trim());

            if (complemento == null || complemento.trim().isEmpty()) {
                stmt.setNull(8, Types.VARCHAR);
            } else {
                stmt.setString(8, complemento.trim());
            }

            stmt.setString(9, porCpf ? cpfLimpo : cnpjLimpo);

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum cliente encontrado com o " + (porCpf ? "CPF" : "CNPJ") + " informado para atualização.");
            }
        }
    }

    // ==========================================
    // DELETE (Remover por CPF ou CNPJ)
    // ==========================================
    public void deletarPorDocumento(String cpf, String cnpj) throws SQLException {
        String cpfLimpo = (cpf != null) ? cpf.replaceAll("\\D", "") : "";
        String cnpjLimpo = (cnpj != null) ? cnpj.replaceAll("\\D", "") : "";

        boolean porCpf = !cpfLimpo.isEmpty();
        if (!porCpf && cnpjLimpo.isEmpty()) {
            throw new SQLException("Informe o CPF ou CNPJ do cliente que deseja excluir.");
        }

        String sql = porCpf ? "DELETE FROM cliente WHERE cpf = ?" : "DELETE FROM cliente WHERE cnpj = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, porCpf ? cpfLimpo : cnpjLimpo);
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Nenhum cliente encontrado com o documento informado para exclusão.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("Não é possível excluir este cliente pois ele possui veículos vinculados no sistema.", e);
            }
            throw e;
        }
    }
}