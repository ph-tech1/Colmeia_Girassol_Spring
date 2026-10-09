package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record AtualizarCriancaRequest(
        String nome,
        LocalDate dataNascimento,
        String alergias,
        String restricoesAlimentares,
        String necessidadesEspeciais
) {
}
