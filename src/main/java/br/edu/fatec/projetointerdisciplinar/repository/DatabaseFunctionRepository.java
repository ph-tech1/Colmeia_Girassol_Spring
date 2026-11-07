package br.edu.fatec.projetointerdisciplinar.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DatabaseFunctionRepository {

    private final JdbcTemplate jdbcTemplate;

    public BigDecimal buscarPercentualPresenca(Integer matriculaCodigo) {
        return jdbcTemplate.queryForObject(
                "SELECT dbo.fn_percentual_presenca_matricula(?)",
                BigDecimal.class,
                matriculaCodigo);
    }

    public List<RegistroFrequencia> listarHistoricoFrequencia(Integer matriculaCodigo) {
        return jdbcTemplate.query("""
                        SELECT matricula_codigo, aluno_codigo, aluno_nome, aula_codigo, data_aula,
                               atividade_dinamica, aula_descricao, turma_codigo, nome_turma,
                               professor_nome, status_presenca, registrado_em, alimentacao,
                               sono, humor, banheiro_fralda, observacoes
                        FROM dbo.fn_historico_frequencia_matricula(?)
                        ORDER BY data_aula DESC, aula_codigo DESC
                        """,
                (resultSet, rowNum) -> new RegistroFrequencia(
                        resultSet.getInt("matricula_codigo"),
                        resultSet.getInt("aluno_codigo"),
                        resultSet.getString("aluno_nome"),
                        resultSet.getInt("aula_codigo"),
                        resultSet.getObject("data_aula", LocalDate.class),
                        resultSet.getString("atividade_dinamica"),
                        resultSet.getString("aula_descricao"),
                        resultSet.getInt("turma_codigo"),
                        resultSet.getString("nome_turma"),
                        resultSet.getString("professor_nome"),
                        resultSet.getString("status_presenca"),
                        resultSet.getTimestamp("registrado_em").toLocalDateTime(),
                        resultSet.getString("alimentacao"),
                        resultSet.getString("sono"),
                        resultSet.getString("humor"),
                        resultSet.getString("banheiro_fralda"),
                        resultSet.getString("observacoes")),
                matriculaCodigo);
    }

    public Optional<ResumoFrequencia> buscarResumoFrequencia(Integer matriculaCodigo) {
        return jdbcTemplate.query("""
                        SELECT matricula_codigo, dias_registrados, presencas, faltas, faltas_justificadas
                        FROM dbo.fn_resumo_frequencia_matricula(?)
                        """,
                (resultSet, rowNum) -> new ResumoFrequencia(
                        resultSet.getInt("matricula_codigo"),
                        resultSet.getLong("dias_registrados"),
                        resultSet.getLong("presencas"),
                        resultSet.getLong("faltas"),
                        resultSet.getLong("faltas_justificadas")),
                matriculaCodigo).stream().findFirst();
    }

    public record RegistroFrequencia(
            Integer matriculaCodigo,
            Integer alunoCodigo,
            String alunoNome,
            Integer aulaCodigo,
            LocalDate dataAula,
            String atividadeDinamica,
            String aulaDescricao,
            Integer turmaCodigo,
            String turmaNome,
            String professorNome,
            String statusPresenca,
            LocalDateTime registradoEm,
            String alimentacao,
            String sono,
            String humor,
            String banheiroFralda,
            String observacoes
    ) {
    }

    public record ResumoFrequencia(
            Integer matriculaCodigo,
            long diasRegistrados,
            long presencas,
            long faltas,
            long faltasJustificadas
    ) {
    }
}
