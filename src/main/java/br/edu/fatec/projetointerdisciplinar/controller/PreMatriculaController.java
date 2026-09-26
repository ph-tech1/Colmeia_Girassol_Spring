package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.dto.PreMatriculaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.PreMatriculaResponse;
import br.edu.fatec.projetointerdisciplinar.service.PreMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pre-matriculas")
@RequiredArgsConstructor
public class PreMatriculaController {

    private final PreMatriculaService preMatriculaService;

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody PreMatriculaRequest request) {
        try {
            PreMatriculaResponse response = preMatriculaService.cadastrar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    private record ErroResponse(String mensagem) {
    }
}
