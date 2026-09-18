package br.edu.fatec.projetointerdisciplinar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavegacaoController {

    // Mapeia http://localhost:8080/Inicio para o arquivo index.html
    @GetMapping("/Inicio")
    public String paginaInicial() {
        return "index"; // Nome do arquivo HTML em src/main/resources/templates/index.html
    }

    // Mantém a rota /Login para a tela padrão
    @GetMapping("/Login")
    public String login() {
        return "html/login";
    }

    // Altera a rota para evitar o conflito com a rota acima
    @GetMapping("/LoginResponsavel") // <--- Altere aqui
    public String paginaLoginResponsavel() {
        return "login-responsavel"; // Ajuste para o nome correto do seu template HTML
    }

    @GetMapping("/Sobre")
    public String sobre() {
        return "sobre"; // Aponta para templates/sobre.html
    }


}