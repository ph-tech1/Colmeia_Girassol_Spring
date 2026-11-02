package br.edu.fatec.projetointerdisciplinar.model;

import br.edu.fatec.projetointerdisciplinar.enums.EstadoCivil;
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

    @Column(name = "local_trabalho", length = 100)
    private String localTrabalho;

    @Column(name = "telefone_trabalho", length = 15)
    private String telefoneTrabalho;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    private EstadoCivil estadoCivil;

    @Column(name = "status", nullable = false)
    private Integer status;
}