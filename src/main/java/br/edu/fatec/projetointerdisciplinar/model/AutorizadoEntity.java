package br.edu.fatec.projetointerdisciplinar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "autorizados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutorizadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "rg", length = 20)
    private String rg;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pessoa_codigo")
    private PessoaEntity pessoa;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public String getNome() {
        return pessoa == null ? nome : pessoa.getNome();
    }

    public String getRg() {
        return pessoa == null ? rg : pessoa.getRg();
    }

    public String getTelefone() {
        return pessoa == null ? telefone : pessoa.getTelefone();
    }
}
