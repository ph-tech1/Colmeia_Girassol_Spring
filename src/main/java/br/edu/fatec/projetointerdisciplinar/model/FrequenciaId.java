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
    private Integer aulaPlanejamentosCodigo;
    private Integer matriculaCodigo;
}