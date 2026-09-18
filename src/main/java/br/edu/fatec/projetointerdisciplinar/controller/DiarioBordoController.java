package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.service.DiarioBordoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/diarios-bordo")
@RequiredArgsConstructor
public class DiarioBordoController {

    private final DiarioBordoService diarioBordoService;

    @GetMapping
    public String listarDiariosBordo(Model model) {
        model.addAttribute("listaDiariosBordo", diarioBordoService.listarTodos());
        return "diarios-bordo/lista";
    }

    @PostMapping("/salvar")
    public String salvarDiarioBordo(@ModelAttribute DiarioBordoEntity diarioBordo) {
        diarioBordoService.salvar(diarioBordo);
        return "redirect:/diarios-bordo";
    }
}
