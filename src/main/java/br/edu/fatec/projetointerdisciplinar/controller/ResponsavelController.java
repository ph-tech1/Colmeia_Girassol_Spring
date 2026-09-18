package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.service.ResponsavelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsaveis")
@RequiredArgsConstructor
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    @GetMapping
    public ResponseEntity<List<ResponsavelEntity>> listarTodos() {
        return ResponseEntity.ok(responsavelService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<ResponsavelEntity> salvar(@RequestBody ResponsavelEntity responsavel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavelService.salvar(responsavel));
    }
}
