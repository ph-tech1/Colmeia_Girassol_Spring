package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.dto.NovoProfessorRequest;
import br.edu.fatec.projetointerdisciplinar.dto.ProfessorAdminResponse;
import br.edu.fatec.projetointerdisciplinar.service.AdminProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/professores")
@RequiredArgsConstructor
public class AdminProfessorController {

    private final AdminProfessorService adminProfessorService;

    @GetMapping
    public ResponseEntity<List<ProfessorAdminResponse>> listar() {
        return ResponseEntity.ok(adminProfessorService.listar());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody NovoProfessorRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(adminProfessorService.cadastrar(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(exception.getMessage()));
        }
    }

    private record ErroResponse(String mensagem) {
    }
}
