package br.edu.fatec.projetointerdisciplinar.dto;

public record AtualizarAutorizadoRequest(
        String nome,
        String rg,
        String telefone
) {
}
