package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.service.AutorizadoBuscaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/autorizados-busca")
@RequiredArgsConstructor
public class AutorizadoBuscaController {

    private final AutorizadoBuscaService autorizadoBuscaService;

    @GetMapping
    public String listarAutorizadosBusca(Model model) {
        model.addAttribute("listaAutorizadosBusca", autorizadoBuscaService.listarTodos());
        return "autorizados-busca/lista";
    }

    @PostMapping("/salvar")
    public String salvarAutorizadoBusca(@ModelAttribute AutorizadoBuscaEntity autorizadoBusca) {
        autorizadoBuscaService.salvar(autorizadoBusca);
        return "redirect:/autorizados-busca";
    }
}
