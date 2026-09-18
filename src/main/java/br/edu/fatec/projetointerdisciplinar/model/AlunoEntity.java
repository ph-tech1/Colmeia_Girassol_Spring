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

    @Column(name = "nome",  nullable = false, length = 100)
    private String nome;

    @Column(name = "data_nascimento" , nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "alergias",  nullable = false, length = 50)
    private String alergias;

    @Column(name = "restricoes_alimentar",   nullable = false, length = 50)
    private String restricoesAlimentar;

    @Column(name = "necessidades_especiais")
    private String necessidadesEspeciais;
}
