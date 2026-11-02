package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record PreMatriculaRequest(
        String nomeResponsavel,
        String cpfResponsavel,
        String rgResponsavel,
        LocalDate dataNascimentoResponsavel,
        String sexoResponsavel,
        String senha,
        String cep,
        String cidade,
        String uf,
        String bairro,
        String endereco,
        String numero,
        String complemento,
        String telefone,
        String localTrabalho,
        String telefoneTrabalho,
        String estadoCivil,
        String email,
        String nomeAluno,
        LocalDate dataNascimentoAluno,
        String sexoAluno,
        Integer parentescoCodigo,
        String alergias,
        String restricoesAlimentares,
        String necessidadesEspeciais
) {
}
