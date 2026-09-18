package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "responsaveis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelEntity {

    @Id
    @Column(name = "pessoa_codigo")
    private Integer pessoaCodigo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "pessoa_codigo")
    private PessoaEntity pessoa;

    @Column(name = "local_trabalho", nullable = false, length = 50)
    private String localTrabalho;

    @Column(name = "telefone_trabalho", nullable = false, length = 15)
    private String telefoneTrabalho;

    @Column(name = "estado_civil", nullable = false, length = 50)
    private String estadoCivil;

    @Column(name = "status", nullable = false)
    private Integer status;
}