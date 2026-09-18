package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.service.PessoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @GetMapping
    public String listarPessoas(Model model) {
        model.addAttribute("listaPessoas", pessoaService.listarTodos());
        return "pessoas/lista";
    }

    @PostMapping("/salvar")
    public String salvarPessoa(@ModelAttribute PessoaEntity pessoa) {
        pessoaService.salvar(pessoa);
        return "redirect:/pessoas";
    }
}
