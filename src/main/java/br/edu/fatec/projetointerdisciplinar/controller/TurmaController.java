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
    public ResponseEntity<?> salvar(@RequestBody TurmaEntity turma) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.salvar(turma));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<?> atualizar(@PathVariable Integer codigo, @RequestBody TurmaEntity turma) {
        try {
            return ResponseEntity.ok(turmaService.atualizar(codigo, turma));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<?> deletar(@PathVariable Integer codigo) {
        try {
            turmaService.deletar(codigo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    private record ErroResponse(String mensagem) {
    }
}
