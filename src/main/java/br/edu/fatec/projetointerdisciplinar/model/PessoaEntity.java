package br.edu.fatec.projetointerdisciplinar.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "rg", length = 20, unique = true)
    private String rg;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "tipo_perfil", nullable = false, length = 20)
    private String tipoPerfil;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "cidade", nullable = false, length = 50)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "endereco", nullable = false, length = 100)
    private String endereco;

    @Column(name = "telefone", nullable = false, length = 15)
    private String telefone;
}