package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    public ResponseEntity<List<MatriculaEntity>> listarTodos() {
        return ResponseEntity.ok(matriculaService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<MatriculaEntity> salvar(@RequestBody MatriculaEntity matricula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matriculaService.salvar(matricula));
    }

    @PatchMapping("/{nr}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Integer nr,
            @RequestBody AtualizarStatusRequest request
    ) {
        try {
            MatriculaEntity matricula = matriculaService.atualizarStatus(
                    nr,
                    request == null ? null : request.status()
            );
            return ResponseEntity.ok(matricula);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    private record AtualizarStatusRequest(Integer status) {
    }

    private record ErroResponse(String mensagem) {
    }
}
