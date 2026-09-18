package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "matriculas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nr;

    @ManyToOne
    @JoinColumn(name = "aluno_codigo", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne
    @JoinColumn(name = "turma_codigo", nullable = false)
    private TurmaEntity turma;

    @Column(name = "data_matricula", nullable = false, length = 50)
    private String dataMatricula;

    @Column(name = "status", nullable = false)
    private Integer status;
}