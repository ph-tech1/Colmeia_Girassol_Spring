package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record NovoProfessorRequest(
        String nome,
        String email,
        String cpf,
        String rg,
        LocalDate dataNascimento,
        String sexo,
        String senha,
        String cep,
        String cidade,
        String uf,
        String bairro,
        String endereco,
        String numero,
        String complemento,
        String telefone,
        String formacao,
        LocalDate dataContratacao
) {
}
