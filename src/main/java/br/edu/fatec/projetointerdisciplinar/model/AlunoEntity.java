package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "alunos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlunoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "sexo", nullable = false, length = 1)
    private String sexo;

    @Column(name = "alergias", length = 100)
    private String alergias;

    @Column(name = "restricoes_alimentares", length = 100)
    private String restricoesAlimentares;

    @Column(name = "necessidades_especiais", columnDefinition = "VARCHAR(MAX)")
    private String necessidadesEspeciais;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private java.time.LocalDateTime criadoEm;
}
