package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @GetMapping
    public String listarTurmas(Model model) {
        model.addAttribute("listaTurmas", turmaService.listarTodos());
        return "turmas/lista";
    }

    @PostMapping("/salvar")
    public String salvarTurma(@ModelAttribute TurmaEntity turma) {
        turmaService.salvar(turma);
        return "redirect:/turmas";
    }
}
