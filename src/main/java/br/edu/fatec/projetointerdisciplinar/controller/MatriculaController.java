package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    public String listarMatriculas(Model model) {
        model.addAttribute("listaMatriculas", matriculaService.listarTodos());
        return "matriculas/lista";
    }

    @PostMapping("/salvar")
    public String salvarMatricula(@ModelAttribute MatriculaEntity matricula) {
        matriculaService.salvar(matricula);
        return "redirect:/matriculas";
    }
}
