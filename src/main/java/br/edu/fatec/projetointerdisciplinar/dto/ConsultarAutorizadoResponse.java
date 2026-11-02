package br.edu.fatec.projetointerdisciplinar.dto;

public record ConsultarAutorizadoResponse(
        boolean encontrado,
        String nomeMascarado
) {
}
