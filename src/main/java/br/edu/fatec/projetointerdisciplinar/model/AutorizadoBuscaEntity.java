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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @ManyToOne
    @JoinColumn(name = "aluno_codigo", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne
    @JoinColumn(name = "responsavel_codigo", nullable = false)
    private ResponsavelEntity responsavel;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "telefone", nullable = false, length = 15)
    private String telefone;

    @Column(name = "grau_parentesco", nullable = false, length = 50)
    private String grauParentesco;
}