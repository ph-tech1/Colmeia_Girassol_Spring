package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "autorizados_busca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutorizadoBuscaEntity {

    @Id
    @Column(name = "codigo")
    private Integer codigo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "codigo")
    private PessoaEntity pessoa;

    @ManyToOne
    @JoinColumn(name = "aluno_codigo", nullable = false)
    private AlunoEntity aluno;

    @Column(name = "grau_parentesco", nullable = false, length = 50)
    private String grauParentesco;
}