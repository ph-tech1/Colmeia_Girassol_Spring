package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "turma_codigo")
    private TurmaEntity turma;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;
}