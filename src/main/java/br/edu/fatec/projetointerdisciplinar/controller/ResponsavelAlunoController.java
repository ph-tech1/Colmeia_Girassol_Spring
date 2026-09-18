package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.service.ResponsavelAlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/responsaveis-alunos")
@RequiredArgsConstructor
public class ResponsavelAlunoController {

    private final ResponsavelAlunoService responsavelAlunoService;

    @GetMapping
    public String listarResponsaveisAlunos(Model model) {
        model.addAttribute("listaResponsaveisAlunos", responsavelAlunoService.listarTodos());
        return "responsaveis-alunos/lista";
    }

    @PostMapping("/salvar")
    public String salvarResponsavelAluno(@ModelAttribute ResponsavelAlunoEntity responsavelAluno) {
        responsavelAlunoService.salvar(responsavelAluno);
        return "redirect:/responsaveis-alunos";
    }
}
