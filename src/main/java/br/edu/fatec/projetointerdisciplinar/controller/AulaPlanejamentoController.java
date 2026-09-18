package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AulaPlanejamentoEntity;
import br.edu.fatec.projetointerdisciplinar.service.AulaPlanejamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/aulas-planejamentos")
@RequiredArgsConstructor
public class AulaPlanejamentoController {

    private final AulaPlanejamentoService aulaPlanejamentoService;

    @GetMapping
    public String listarAulasPlanejamentos(Model model) {
        model.addAttribute("listaAulasPlanejamentos", aulaPlanejamentoService.listarTodos());
        return "aulas-planejamentos/lista";
    }

    @PostMapping("/salvar")
    public String salvarAulaPlanejamento(@ModelAttribute AulaPlanejamentoEntity aulaPlanejamento) {
        aulaPlanejamentoService.salvar(aulaPlanejamento);
        return "redirect:/aulas-planejamentos";
    }
}
