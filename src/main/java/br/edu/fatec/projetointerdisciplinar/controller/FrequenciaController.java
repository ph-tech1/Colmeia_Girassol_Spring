package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.service.FrequenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/frequencias")
@RequiredArgsConstructor
public class FrequenciaController {

    private final FrequenciaService frequenciaService;

    @GetMapping
    public ResponseEntity<List<FrequenciaEntity>> listarTodos() {
        return ResponseEntity.ok(frequenciaService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<FrequenciaEntity> salvar(@RequestBody FrequenciaEntity frequencia) {
        return ResponseEntity.status(HttpStatus.CREATED).body(frequenciaService.salvar(frequencia));
    }
}
