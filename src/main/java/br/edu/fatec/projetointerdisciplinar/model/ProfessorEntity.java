package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorEntity {

    @Id
    @Column(name = "pessoa_codigo")
    private Integer pessoaCodigo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "pessoa_codigo")
    private PessoaEntity pessoa;

    @Column(name = "data_contratacao", nullable = false, length = 50)
    private String dataContratacao;

    @Column(name = "formacao", nullable = false, length = 50)
    private String formacao;

    @Column(name = "status", nullable = false)
    private Integer status;
}