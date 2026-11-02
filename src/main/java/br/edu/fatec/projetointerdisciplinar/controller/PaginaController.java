package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.repository.ParentescoRepository;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PaginaController {

    private final ParentescoRepository parentescoRepository;

    public PaginaController(ParentescoRepository parentescoRepository) {
        this.parentescoRepository = parentescoRepository;
    }

    @GetMapping({"/", "/index.html"})
    public String inicio() {
        return "index";
    }

    @GetMapping("/html/{pagina}.html")
    public String pagina(@PathVariable String pagina, Model model) {
        if ("nova_senha".equals(pagina)) {
            return "redirect:/html/login.html";
        }
        if ("cadastro".equals(pagina) || "cadastro_professor".equals(pagina)) {
            model.addAttribute("unidadesFederativas", UnidadeFederativa.values());
        }
        if ("cadastro".equals(pagina)) {
            model.addAttribute("parentescos", parentescoRepository.findAll());
        }
        return "html/" + pagina;
    }
}