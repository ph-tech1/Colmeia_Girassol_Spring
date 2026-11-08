package br.edu.fatec.projetointerdisciplinar.dto;

public record AtualizarAutorizadoResponsavelRequest(
        String nome,
        String rg,
        String telefone,
        Integer parentescoCodigo
) {
}
