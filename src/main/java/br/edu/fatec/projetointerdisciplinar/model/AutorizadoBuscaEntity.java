package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "autorizados_busca",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_ab_autorizado_aluno",
                columnNames = {"autorizado_codigo", "aluno_codigo"}
        )
)
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
    @JoinColumn(name = "autorizado_codigo", nullable = false)
    private AutorizadoEntity autorizado;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "responsavel_codigo", referencedColumnName = "responsavel_codigo", nullable = false),
            @JoinColumn(name = "aluno_codigo", referencedColumnName = "aluno_codigo", nullable = false)
    })
    private ResponsavelAlunoEntity vinculo;

    @ManyToOne
    @JoinColumn(name = "parentesco_codigo", nullable = false)
    private ParentescoEntity parentesco;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}