package br.edu.fatec.projetointerdisciplinar.dto;

public record RegistrarFrequenciaRequest(
        Integer aulaPlanejamentosCodigo,
        Integer matriculaCodigo,
        String statusPresenca
) {
}
