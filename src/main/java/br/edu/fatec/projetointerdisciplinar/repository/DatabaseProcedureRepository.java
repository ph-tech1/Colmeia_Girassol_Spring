package br.edu.fatec.projetointerdisciplinar.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DatabaseProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public void decidirMatricula(Integer matriculaCodigo, Integer novoStatus, String motivoRejeicao) {
        jdbcTemplate.execute("{call dbo.sp_decidir_matricula(?, ?, ?)}",
                (CallableStatementCallback<Void>) statement -> {
                    statement.setInt(1, matriculaCodigo);
                    statement.setInt(2, novoStatus);
                    statement.setString(3, motivoRejeicao);
                    statement.execute();
                    return null;
                });
    }

    public void atribuirTurma(Integer matriculaCodigo, Integer turmaCodigo) {
        jdbcTemplate.execute("{call dbo.sp_atribuir_turma_matricula(?, ?)}",
                (CallableStatementCallback<Void>) statement -> {
                    statement.setInt(1, matriculaCodigo);
                    statement.setInt(2, turmaCodigo);
                    statement.execute();
                    return null;
                });
    }

    public void registrarFrequencia(Integer aulaCodigo, Integer matriculaCodigo,
                                    Integer turmaCodigo, String statusPresenca) {
        jdbcTemplate.execute("{call dbo.sp_registrar_frequencia(?, ?, ?, ?)}",
                (CallableStatementCallback<Void>) statement -> {
                    statement.setInt(1, aulaCodigo);
                    statement.setInt(2, matriculaCodigo);
                    statement.setInt(3, turmaCodigo);
                    statement.setString(4, statusPresenca);
                    statement.execute();
                    return null;
                });
    }
}
