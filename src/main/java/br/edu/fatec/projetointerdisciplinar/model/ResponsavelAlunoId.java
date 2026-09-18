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
public class ResponsavelAlunoId implements Serializable {
    private Integer responsavelCodigo;
    private Integer alunoCodigo;
}