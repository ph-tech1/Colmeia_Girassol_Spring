package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.service.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    // Exibe a página com a lista de alunos
    @GetMapping
    public String listarAlunos(Model model) {
        model.addAttribute("listaAlunos", alunoService.listarTodos());
        return "alunos/lista"; // Caminho da página HTML em src/main/resources/templates/alunos/lista.html
    }

    // Recebe os dados do formulário de cadastro e chama o Service
    @PostMapping("/salvar")
    public String salvarAluno(@ModelAttribute AlunoEntity aluno) {
        alunoService.salvar(aluno);
        return "redirect:/alunos"; // Redireciona de volta para a listagem
    }
}