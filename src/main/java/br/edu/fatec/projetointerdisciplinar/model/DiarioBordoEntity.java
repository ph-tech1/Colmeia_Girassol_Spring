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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne
    @JoinColumn(name = "matricula_nr", nullable = false)
    private MatriculaEntity matricula;

    @Column(name = "data_registro", nullable = false, length = 50)
    private String dataRegistro;

    @Column(name = "compareceu", nullable = false)
    private Boolean compareceu;

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
}