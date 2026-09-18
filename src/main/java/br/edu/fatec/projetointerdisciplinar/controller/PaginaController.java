package br.edu.fatec.projetointerdisciplinar.controller;

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
    public String pagina(@PathVariable String pagina) {
        return "html/" + pagina;
    }
}