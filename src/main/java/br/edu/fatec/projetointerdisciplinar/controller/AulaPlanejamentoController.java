package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AulaPlanejamentoEntity;
import br.edu.fatec.projetointerdisciplinar.service.AulaPlanejamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aulas-planejamentos")
@RequiredArgsConstructor
public class AulaPlanejamentoController {

    private final AulaPlanejamentoService aulaPlanejamentoService;

    @GetMapping
    public ResponseEntity<List<AulaPlanejamentoEntity>> listarTodos() {
        return ResponseEntity.ok(aulaPlanejamentoService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<AulaPlanejamentoEntity> salvar(@RequestBody AulaPlanejamentoEntity aulaPlanejamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aulaPlanejamentoService.salvar(aulaPlanejamento));
    }
}
