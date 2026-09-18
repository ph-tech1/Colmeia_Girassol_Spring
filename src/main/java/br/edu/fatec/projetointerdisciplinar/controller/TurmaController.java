package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @GetMapping
    public ResponseEntity<List<TurmaEntity>> listarTodos() {
        return ResponseEntity.ok(turmaService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<TurmaEntity> salvar(@RequestBody TurmaEntity turma) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.salvar(turma));
    }
}
