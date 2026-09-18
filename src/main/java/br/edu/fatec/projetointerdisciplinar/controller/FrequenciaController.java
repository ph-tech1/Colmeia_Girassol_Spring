package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.service.FrequenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/frequencias")
@RequiredArgsConstructor
public class FrequenciaController {

    private final FrequenciaService frequenciaService;

    @GetMapping
    public String listarFrequencias(Model model) {
        model.addAttribute("listaFrequencias", frequenciaService.listarTodos());
        return "frequencias/lista";
    }

    @PostMapping("/salvar")
    public String salvarFrequencia(@ModelAttribute FrequenciaEntity frequencia) {
        frequenciaService.salvar(frequencia);
        return "redirect:/frequencias";
    }
}
