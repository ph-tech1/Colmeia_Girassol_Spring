package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "frequencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FrequenciaEntity {

    @EmbeddedId
    private FrequenciaId id;

    @ManyToOne
    @MapsId("aulaPlanejamentosCodigo")
    @JoinColumn(name = "aula_planejamentos_codigo")
    private AulaPlanejamentoEntity aulaPlanejamento;

    @ManyToOne
    @MapsId("matriculaCodigo")
    @JoinColumn(name = "matricula_codigo")
    private MatriculaEntity matricula;

    @Column(name = "status_presenca", nullable = false, length = 1)
    private String statusPresenca;
}