package br.edu.fatec.projetointerdisciplinar.dto;

public record AdicionarAutorizadoBuscaRequest(
        Integer alunoCodigo,
        String nome,
        String telefone,
        String grauParentesco
) {
}
