package br.edu.fatec.projetointerdisciplinar.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DatabaseViewRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ResumoFrequencia> listarResumoFrequenciaPorProfessor(Integer professorCodigo) {
        return jdbcTemplate.query("""
                        SELECT resumo.aluno_codigo, resumo.aluno_nome, turma.matricula_codigo,
                               resumo.dias_registrados, resumo.presencas, resumo.faltas,
                               resumo.faltas_justificadas, resumo.percentual_presenca
                        FROM dbo.vw_resumo_frequencia_aluno AS resumo
                        JOIN dbo.vw_alunos_por_turma AS turma ON turma.aluno_codigo = resumo.aluno_codigo
                        WHERE turma.professor_codigo = ?
                        ORDER BY turma.nome_turma, resumo.aluno_nome
                        """,
                (resultSet, rowNum) -> new ResumoFrequencia(
                        resultSet.getInt("aluno_codigo"),
                        resultSet.getString("aluno_nome"),
                        resultSet.getInt("matricula_codigo"),
                        resultSet.getInt("dias_registrados"),
                        resultSet.getInt("presencas"),
                        resultSet.getInt("faltas"),
                        resultSet.getInt("faltas_justificadas"),
                        resultSet.getBigDecimal("percentual_presenca")),
                professorCodigo);
    }

    public List<AlunoTurma> listarAlunosPorProfessor(Integer professorCodigo) {
        return jdbcTemplate.query("""
                        SELECT turma_codigo, nome_turma, ano, grau, matricula_codigo,
                               aluno_codigo, aluno_nome, responsavel_nome, responsavel_telefone
                        FROM dbo.vw_alunos_por_turma
                        WHERE professor_codigo = ?
                        ORDER BY nome_turma, aluno_nome
                        """,
                (resultSet, rowNum) -> new AlunoTurma(
                        resultSet.getInt("turma_codigo"),
                        resultSet.getString("nome_turma"),
                        resultSet.getInt("ano"),
                        resultSet.getString("grau"),
                        resultSet.getInt("matricula_codigo"),
                        resultSet.getInt("aluno_codigo"),
                        resultSet.getString("aluno_nome"),
                        resultSet.getString("responsavel_nome"),
                        resultSet.getString("responsavel_telefone")),
                professorCodigo);
    }

    public List<ResumoFrequencia> listarResumoFrequenciaPorResponsavel(Integer responsavelCodigo) {
        return jdbcTemplate.query("""
                        SELECT resumo.aluno_codigo, resumo.aluno_nome, CAST(NULL AS INT) AS matricula_codigo,
                               resumo.dias_registrados, resumo.presencas, resumo.faltas,
                               resumo.faltas_justificadas, resumo.percentual_presenca
                        FROM dbo.vw_resumo_frequencia_aluno AS resumo
                        JOIN dbo.responsavel_aluno AS vinculo ON vinculo.aluno_codigo = resumo.aluno_codigo
                        WHERE vinculo.responsavel_codigo = ?
                        ORDER BY resumo.aluno_nome
                        """,
                (resultSet, rowNum) -> new ResumoFrequencia(
                        resultSet.getInt("aluno_codigo"),
                        resultSet.getString("aluno_nome"),
                        (Integer) resultSet.getObject("matricula_codigo"),
                        resultSet.getInt("dias_registrados"),
                        resultSet.getInt("presencas"),
                        resultSet.getInt("faltas"),
                        resultSet.getInt("faltas_justificadas"),
                        resultSet.getBigDecimal("percentual_presenca")),
                responsavelCodigo);
    }

    public List<DiarioAluno> listarDiarioPorResponsavel(Integer responsavelCodigo) {
        return jdbcTemplate.query("""
                        SELECT diario.*
                        FROM dbo.vw_diario_bordo_aluno AS diario
                        JOIN dbo.responsavel_aluno AS vinculo ON vinculo.aluno_codigo = diario.aluno_codigo
                        WHERE vinculo.responsavel_codigo = ?
                        ORDER BY diario.data_aula DESC, diario.aula_codigo DESC
                        """,
                (resultSet, rowNum) -> mapDiario(resultSet),
                responsavelCodigo);
    }

    public List<DiarioAluno> listarDiarioPorAlunoResponsavel(Integer alunoCodigo, Integer responsavelCodigo) {
        return jdbcTemplate.query("""
                        SELECT diario.*
                        FROM dbo.vw_diario_bordo_aluno AS diario
                        JOIN dbo.responsavel_aluno AS vinculo
                          ON vinculo.aluno_codigo = diario.aluno_codigo
                        WHERE diario.aluno_codigo = ?
                          AND vinculo.responsavel_codigo = ?
                        ORDER BY diario.data_aula DESC, diario.aula_codigo DESC
                        """,
                (resultSet, rowNum) -> mapDiario(resultSet),
                alunoCodigo,
                responsavelCodigo);
    }

    public Optional<ResumoFrequencia> buscarResumoFrequenciaPorAlunoResponsavel(
            Integer alunoCodigo,
            Integer responsavelCodigo
    ) {
        return jdbcTemplate.query("""
                        SELECT resumo.aluno_codigo, resumo.aluno_nome, CAST(NULL AS INT) AS matricula_codigo,
                               resumo.dias_registrados, resumo.presencas, resumo.faltas,
                               resumo.faltas_justificadas, resumo.percentual_presenca
                        FROM dbo.vw_resumo_frequencia_aluno AS resumo
                        JOIN dbo.responsavel_aluno AS vinculo ON vinculo.aluno_codigo = resumo.aluno_codigo
                        WHERE resumo.aluno_codigo = ?
                          AND vinculo.responsavel_codigo = ?
                        """,
                (resultSet, rowNum) -> new ResumoFrequencia(
                        resultSet.getInt("aluno_codigo"),
                        resultSet.getString("aluno_nome"),
                        (Integer) resultSet.getObject("matricula_codigo"),
                        resultSet.getInt("dias_registrados"),
                        resultSet.getInt("presencas"),
                        resultSet.getInt("faltas"),
                        resultSet.getInt("faltas_justificadas"),
                        resultSet.getBigDecimal("percentual_presenca")),
                alunoCodigo,
                responsavelCodigo).stream().findFirst();
    }

    private DiarioAluno mapDiario(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new DiarioAluno(
                resultSet.getInt("aluno_codigo"),
                resultSet.getInt("matricula_codigo"),
                resultSet.getInt("turma_codigo"),
                resultSet.getString("nome_turma"),
                resultSet.getInt("aula_codigo"),
                resultSet.getDate("data_aula").toLocalDate(),
                resultSet.getString("atividade_dinamica"),
                resultSet.getString("professor_nome"),
                resultSet.getString("status_presenca"),
                resultSet.getString("humor"),
                resultSet.getString("alimentacao"),
                resultSet.getString("sono"),
                resultSet.getString("banheiro_fralda"),
                resultSet.getString("observacoes"));
    }

    public record ResumoFrequencia(
            Integer alunoCodigo,
            String alunoNome,
            Integer matriculaCodigo,
            Integer diasRegistrados,
            Integer presencas,
            Integer faltas,
            Integer faltasJustificadas,
            BigDecimal percentualPresenca
    ) {
    }

    public record AlunoTurma(
            Integer turmaCodigo,
            String nomeTurma,
            Integer ano,
            String grau,
            Integer matriculaCodigo,
            Integer alunoCodigo,
            String alunoNome,
            String responsavelNome,
            String responsavelTelefone
    ) {
    }

    public record DiarioAluno(
            Integer alunoCodigo,
            Integer matriculaCodigo,
            Integer turmaCodigo,
            String nomeTurma,
            Integer aulaCodigo,
            LocalDate dataAula,
            String atividadeDinamica,
            String professorNome,
            String statusPresenca,
            String humor,
            String alimentacao,
            String sono,
            String banheiroFralda,
            String observacoes
    ) {
    }
}
