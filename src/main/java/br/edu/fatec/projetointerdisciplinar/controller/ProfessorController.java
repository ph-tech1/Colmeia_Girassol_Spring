package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professores")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping
    public ResponseEntity<List<ProfessorEntity>> listarTodos() {
        return ResponseEntity.ok(professorService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<ProfessorEntity> salvar(@RequestBody ProfessorEntity professor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.salvar(professor));
    }
}
