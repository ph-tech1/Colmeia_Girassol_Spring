package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "responsavel_aluno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelAlunoEntity {

    @EmbeddedId
    private ResponsavelAlunoId id;

    @ManyToOne
    @MapsId("responsavelCodigo")
    @JoinColumn(name = "responsavel_codigo")
    private ResponsavelEntity responsavel;

    @ManyToOne
    @MapsId("alunoCodigo")
    @JoinColumn(name = "aluno_codigo")
    private AlunoEntity aluno;

    @Column(name = "grau_parentesco", nullable = false, length = 20)
    private String grauParentesco;

    @Column(name = "esp_financeiro", nullable = false)
    private Boolean espFinanceiro;

    @Column(name = "ordem_contato", nullable = false)
    private Integer ordemContato;
}