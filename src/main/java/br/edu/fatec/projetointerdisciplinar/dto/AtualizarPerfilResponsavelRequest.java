package br.edu.fatec.projetointerdisciplinar.dto;

public record AtualizarPerfilResponsavelRequest(
        String nome,
        String telefone,
        String sexo,
        String cep,
        String cidade,
        String uf,
        String bairro,
        String endereco,
        String numero,
        String complemento,
        String localTrabalho,
        String telefoneTrabalho,
        String estadoCivil
) {
}
