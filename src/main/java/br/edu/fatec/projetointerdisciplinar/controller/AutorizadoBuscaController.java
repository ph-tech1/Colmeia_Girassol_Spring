package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.service.AutorizadoBuscaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autorizados-busca")
@RequiredArgsConstructor
public class AutorizadoBuscaController {

    private final AutorizadoBuscaService autorizadoBuscaService;

    @GetMapping
    public ResponseEntity<List<AutorizadoBuscaEntity>> listarTodos() {
        return ResponseEntity.ok(autorizadoBuscaService.listarTodos());
    }

}
