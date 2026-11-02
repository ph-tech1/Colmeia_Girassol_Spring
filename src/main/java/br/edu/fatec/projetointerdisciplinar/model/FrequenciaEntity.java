package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "frequencias", uniqueConstraints = @UniqueConstraint(
        name = "UQ_frequencias_status",
        columnNames = {"aula_planejamentos_codigo", "matricula_codigo", "status_presenca"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FrequenciaEntity {

    @EmbeddedId
    private FrequenciaId id;

    @ManyToOne
    @MapsId("aulaPlanejamentosCodigo")
    @JoinColumn(name = "aula_planejamentos_codigo", nullable = false)
    private AulaPlanejamentoEntity aulaPlanejamento;

    @ManyToOne
    @MapsId("matriculaCodigo")
    @JoinColumn(name = "matricula_codigo", nullable = false)
    private MatriculaEntity matricula;

    @Column(name = "status_presenca", nullable = false, length = 1)
    private String statusPresenca;

    @Column(name = "turma_codigo", nullable = false)
    private Integer turmaCodigo;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private java.time.LocalDateTime criadoEm;
}