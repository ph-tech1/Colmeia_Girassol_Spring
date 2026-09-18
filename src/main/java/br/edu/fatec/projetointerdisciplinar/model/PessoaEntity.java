package br.edu.fatec.projetointerdisciplinar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "pessoas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PessoaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "rg", nullable = false, length = 8, unique = true)
    private String rg;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "senha", nullable = false, length = 50)
    private String senha;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "cidade", nullable = false, length = 50)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "endereco", nullable = false, length = 100, unique = true)
    private String endereco;

    @Column(name = "telefone", nullable = false, length = 15)
    private String telefone;
}