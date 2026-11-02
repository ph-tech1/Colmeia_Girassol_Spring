package br.edu.fatec.projetointerdisciplinar.dto;

public record AdicionarAutorizadoBuscaRequest(
        Integer alunoCodigo,
        String cpf,
        String nome,
        String rg,
        String telefone,
        Integer parentescoCodigo,
        boolean confirmarPessoaExistente
) {
}
