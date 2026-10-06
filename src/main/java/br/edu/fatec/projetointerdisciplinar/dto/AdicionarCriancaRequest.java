package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record AdicionarCriancaRequest(
        String nome,
        LocalDate dataNascimento,
        String grauParentesco,
        String alergias,
        String restricoesAlimentares,
        String necessidadesEspeciais
) {
}
