package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PaginaController {

    @GetMapping({"/", "/index.html"})
    public String inicio() {
        return "index";
    }

    @GetMapping("/html/{pagina}.html")
    public String pagina(@PathVariable String pagina, Model model) {
        if ("cadastro".equals(pagina)) {
            model.addAttribute("unidadesFederativas", UnidadeFederativa.values());
        }
        return "html/" + pagina;
    }
}