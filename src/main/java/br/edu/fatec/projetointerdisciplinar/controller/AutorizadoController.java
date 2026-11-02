package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.dto.AtualizarAutorizadoRequest;
import br.edu.fatec.projetointerdisciplinar.service.AutorizadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autorizados")
@RequiredArgsConstructor
public class AutorizadoController {

    private final AutorizadoService autorizadoService;

    @PutMapping("/{codigo}")
    public ResponseEntity<?> atualizar(
            @PathVariable Integer codigo,
            @RequestBody AtualizarAutorizadoRequest request
    ) {
        try {
            autorizadoService.atualizar(codigo, request);
            return ResponseEntity.ok(new SucessoResponse("Dados da pessoa autorizada atualizados."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    private record SucessoResponse(String mensagem) {
    }

    private record ErroResponse(String mensagem) {
    }
}
