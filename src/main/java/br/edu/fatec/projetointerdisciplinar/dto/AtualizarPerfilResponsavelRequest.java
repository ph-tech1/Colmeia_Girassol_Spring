package br.edu.fatec.projetointerdisciplinar.dto;

public record AtualizarPerfilResponsavelRequest(
        String nome,
        String telefone,
        String cep,
        String cidade,
        String uf,
        String endereco,
        String localTrabalho,
        String telefoneTrabalho,
        String estadoCivil
) {
}
