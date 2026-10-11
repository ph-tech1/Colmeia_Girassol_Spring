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

    @EmbeddedId
    private AutorizadoBuscaId id;

    @ManyToOne
    @MapsId("codigo")
    @JoinColumn(name = "codigo")
    private PessoaEntity pessoa;

    @ManyToOne
    @MapsId("alunoCodigo")
    @JoinColumn(name = "aluno_codigo", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne
    @MapsId("responsavelCodigo")
    @JoinColumn(name = "responsavel_codigo", nullable = false)
    private ResponsavelEntity responsavel;

    @Column(name = "grau_parentesco", nullable = false, length = 50)
    private String grauParentesco;
}