package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "turmas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne
    @JoinColumn(name = "professor_codigo", nullable = false)
    private ProfessorEntity professor;

    @Column(name = "nome_turma", nullable = false, length = 50)
    private String nomeTurma;

    @Column(name = "ano", nullable = false, length = 50)
    private String ano;

    @Column(name = "grau", nullable = false, length = 50)
    private String grau;
}