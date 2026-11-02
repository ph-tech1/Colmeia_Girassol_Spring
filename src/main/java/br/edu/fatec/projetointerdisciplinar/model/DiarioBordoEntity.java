package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diario_bordo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiarioBordoEntity {

    @EmbeddedId
    private FrequenciaId id;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "aula_planejamentos_codigo", referencedColumnName = "aula_planejamentos_codigo",
                    insertable = false, updatable = false),
            @JoinColumn(name = "matricula_codigo", referencedColumnName = "matricula_codigo",
                    insertable = false, updatable = false),
            @JoinColumn(name = "status_presenca", referencedColumnName = "status_presenca",
                    insertable = false, updatable = false)
    })
    private FrequenciaEntity frequencia;

    @Column(name = "status_presenca", nullable = false, length = 1, insertable = false, updatable = false)
    private String statusPresenca = "P";

    @ManyToOne
    @JoinColumn(name = "registrado_por", nullable = false)
    private ProfessorEntity registradoPor;

    @Column(name = "alimentacao", length = 50)
    private String alimentacao;

    @Column(name = "sono", length = 50)
    private String sono;

    @Column(name = "humor", length = 50)
    private String humor;

    @Column(name = "banheiro_fralda", length = 50)
    private String banheiroFralda;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private java.time.LocalDateTime criadoEm;
}