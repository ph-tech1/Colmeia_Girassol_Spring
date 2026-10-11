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
public class AutorizadoBuscaId implements Serializable {
    private Integer codigo;
    private Integer alunoCodigo;
    private Integer responsavelCodigo;
}
