package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "aulas_planejamentos", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_aulas_codigo_turma", columnNames = {"codigo", "turma_codigo"}),
        @UniqueConstraint(name = "UQ_aulas_turma_data", columnNames = {"turma_codigo", "data_aula"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AulaPlanejamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne
    @JoinColumn(name = "professor_codigo", nullable = false)
    private ProfessorEntity professor;

    @ManyToOne
    @JoinColumn(name = "turma_codigo", nullable = false)
    private TurmaEntity turma;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Column(name = "atividade_dinamica", nullable = false, length = 200)
    private String atividadeDinamica;

    @Column(name = "descricao", nullable = false, columnDefinition = "VARCHAR(MAX)")
    private String descricao;

    @Column(name = "status", nullable = false)
    private Integer status;
}