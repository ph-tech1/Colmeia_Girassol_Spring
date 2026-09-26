package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record PreMatriculaRequest(
        String nomeResponsavel,
        String cpfResponsavel,
        String rgResponsavel,
        LocalDate dataNascimentoResponsavel,
        String senha,
        String cep,
        String cidade,
        String uf,
        String endereco,
        String telefone,
        String localTrabalho,
        String telefoneTrabalho,
        String estadoCivil,
        String email,
        String nomeAluno,
        LocalDate dataNascimentoAluno,
        String grauParentesco,
        String alergias,
        String restricoesAlimentares,
        String necessidadesEspeciais
) {
}
