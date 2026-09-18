package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.service.DiarioBordoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diarios-bordo")
@RequiredArgsConstructor
public class DiarioBordoController {

    private final DiarioBordoService diarioBordoService;

    @GetMapping
    public ResponseEntity<List<DiarioBordoEntity>> listarTodos() {
        return ResponseEntity.ok(diarioBordoService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<DiarioBordoEntity> salvar(@RequestBody DiarioBordoEntity diarioBordo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diarioBordoService.salvar(diarioBordo));
    }
}
