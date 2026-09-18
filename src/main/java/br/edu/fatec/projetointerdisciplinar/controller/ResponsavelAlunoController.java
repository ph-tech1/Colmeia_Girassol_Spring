package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.service.ResponsavelAlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsaveis-alunos")
@RequiredArgsConstructor
public class ResponsavelAlunoController {

    private final ResponsavelAlunoService responsavelAlunoService;

    @GetMapping
    public ResponseEntity<List<ResponsavelAlunoEntity>> listarTodos() {
        return ResponseEntity.ok(responsavelAlunoService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<ResponsavelAlunoEntity> salvar(@RequestBody ResponsavelAlunoEntity responsavelAluno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavelAlunoService.salvar(responsavelAluno));
    }
}
