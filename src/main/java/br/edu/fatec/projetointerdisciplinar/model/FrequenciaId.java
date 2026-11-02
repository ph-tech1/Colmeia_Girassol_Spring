package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FrequenciaId implements Serializable {
    @jakarta.persistence.Column(name = "aula_planejamentos_codigo")
    private Integer aulaPlanejamentosCodigo;
    @jakarta.persistence.Column(name = "matricula_codigo")
    private Integer matriculaCodigo;
}