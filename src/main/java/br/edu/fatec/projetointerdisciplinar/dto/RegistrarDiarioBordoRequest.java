package br.edu.fatec.projetointerdisciplinar.dto;

public record RegistrarDiarioBordoRequest(
        Integer aulaPlanejamentosCodigo,
        Integer matriculaCodigo,
        String alimentacao,
        String sono,
        String humor,
        String banheiroFralda,
        String observacoes
) {
}
