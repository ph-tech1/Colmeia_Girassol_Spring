package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.service.ResponsavelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/responsaveis")
@RequiredArgsConstructor
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    @GetMapping
    public String listarResponsaveis(Model model) {
        model.addAttribute("listaResponsaveis", responsavelService.listarTodos());
        return "responsaveis/lista";
    }

    @PostMapping("/salvar")
    public String salvarResponsavel(@ModelAttribute ResponsavelEntity responsavel) {
        responsavelService.salvar(responsavel);
        return "redirect:/responsaveis";
    }
}
