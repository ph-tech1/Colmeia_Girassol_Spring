package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parentescos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentescoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "descricao", nullable = false, length = 50, unique = true)
    private String descricao;
}
