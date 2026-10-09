package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.dto.AdicionarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarPerfilResponsavelRequest;
import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.service.PortalResponsavelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portal-responsavel")
@RequiredArgsConstructor
public class PortalResponsavelController {

    private final PortalResponsavelService portalResponsavelService;

    @PutMapping("/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @RequestBody AtualizarPerfilResponsavelRequest request,
            Authentication authentication
    ) {
        try {
            PessoaEntity pessoa = portalResponsavelService.atualizarPerfil(authentication.getName(), request);
            return ResponseEntity.ok(new PerfilResponse(pessoa.getNome(), pessoa.getTelefone()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    @PostMapping("/criancas")
    public ResponseEntity<?> adicionarCrianca(
            @RequestBody AdicionarCriancaRequest request,
            Authentication authentication
    ) {
        try {
            AlunoEntity aluno = portalResponsavelService.adicionarCrianca(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CriancaResponse(aluno.getCodigo(), "Criança vinculada com sucesso."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    @PutMapping("/criancas/{codigo}")
    public ResponseEntity<?> atualizarCrianca(
            @PathVariable Integer codigo,
            @RequestBody AtualizarCriancaRequest request,
            Authentication authentication
    ) {
        try {
            AlunoEntity aluno = portalResponsavelService.atualizarCrianca(authentication.getName(), codigo, request);
            return ResponseEntity.ok(new CriancaResponse(aluno.getCodigo(), "Dados da criança atualizados com sucesso."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
        }
    }

    private record PerfilResponse(String nome, String telefone) {
    }

    private record CriancaResponse(Integer codigo, String mensagem) {
    }

    private record ErroResponse(String mensagem) {
    }
}
