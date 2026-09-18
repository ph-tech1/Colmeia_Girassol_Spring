package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping
    public String listarProfessores(Model model) {
        model.addAttribute("listaProfessores", professorService.listarTodos());
        return "professores/lista";
    }

    @PostMapping("/salvar")
    public String salvarProfessor(@ModelAttribute ProfessorEntity professor) {
        professorService.salvar(professor);
        return "redirect:/professores";
    }
}
