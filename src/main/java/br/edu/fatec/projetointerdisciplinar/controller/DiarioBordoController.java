package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.dto.RegistrarDiarioBordoRequest;
import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.service.DiarioBordoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> salvar(
            @RequestBody RegistrarDiarioBordoRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(diarioBordoService.salvar(request, authentication.getName()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    @DeleteMapping("/{aulaCodigo}/{matriculaCodigo}")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer aulaCodigo,
            @PathVariable Integer matriculaCodigo,
            Authentication authentication
    ) {
        diarioBordoService.deletar(aulaCodigo, matriculaCodigo, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private record ErroResponse(String mensagem) {
    }
}
