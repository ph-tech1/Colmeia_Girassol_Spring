package br.edu.fatec.projetointerdisciplinar.dto;

import java.time.LocalDate;

public record ProfessorAdminResponse(
        Integer pessoaCodigo,
        String nome,
        String email,
        String formacao,
        LocalDate dataContratacao,
        Integer status
) {
}
