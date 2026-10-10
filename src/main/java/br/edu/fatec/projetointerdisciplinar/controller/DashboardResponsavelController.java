package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.DiarioBordoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelAlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardResponsavelController {

    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final DiarioBordoRepository diarioBordoRepository;
    private final AutorizadoBuscaRepository autorizadoBuscaRepository;

    @GetMapping("/html/dashboard_responsavel.html")
    @Transactional(readOnly = true)
    public String dashboard(Authentication authentication, Model model) {
        PessoaEntity pessoa = buscarPessoaAutenticada(authentication);
        ResponsavelEntity responsavel = buscarResponsavel(pessoa);

        List<AlunoDashboard> alunos = new ArrayList<>();
        List<ContatoAutorizado> autorizados = new ArrayList<>();

        for (ResponsavelAlunoEntity vinculo : responsavelAlunoRepository
                .findByIdResponsavelCodigo(responsavel.getPessoaCodigo())) {
            AlunoEntity aluno = vinculo.getAluno();
            MatriculaEntity matricula = matriculaRepository.findByAlunoCodigoOrderByNrDesc(aluno.getCodigo())
                    .stream()
                    .filter(item -> item.getStatus() != null && item.getStatus() == 0)
                    .findFirst()
                    .orElse(null);

            DiarioBordoEntity diario = matricula == null ? null
                    : diarioBordoRepository.findByMatriculaNrOrderByCodigoDesc(matricula.getNr())
                    .stream()
                    .findFirst()
                    .orElse(null);

            String professorNome = matricula == null ? null
                    : matricula.getTurma().getProfessor().getPessoa().getNome();
            alunos.add(new AlunoDashboard(aluno, vinculo.getGrauParentesco(), matricula, professorNome, diario));

            for (AutorizadoBuscaEntity autorizado : autorizadoBuscaRepository.findByAlunoCodigo(aluno.getCodigo())) {
                PessoaEntity pessoaAutorizada = autorizado.getPessoa();
                autorizados.add(new ContatoAutorizado(
                        aluno.getNome(),
                        pessoaAutorizada.getNome(),
                        autorizado.getGrauParentesco(),
                        pessoaAutorizada.getTelefone()
                ));
            }
        }

        model.addAttribute("responsavel", pessoa);
        model.addAttribute("cadastroResponsavel", responsavel);
        model.addAttribute("unidadesFederativas", UnidadeFederativa.values());
        model.addAttribute("alunos", alunos);
        model.addAttribute("autorizados", autorizados);
        return "html/dashboard_responsavel";
    }

    @GetMapping("/html/perfil-responsavel")
    @Transactional(readOnly = true)
    public String editarPerfil(Authentication authentication, Model model) {
        PessoaEntity pessoa = buscarPessoaAutenticada(authentication);
        model.addAttribute("responsavel", pessoa);
        model.addAttribute("cadastroResponsavel", buscarResponsavel(pessoa));
        model.addAttribute("unidadesFederativas", UnidadeFederativa.values());
        return "html/perfil_responsavel";
    }

    @GetMapping("/html/adicionar-crianca")
    @Transactional(readOnly = true)
    public String adicionarCrianca(Authentication authentication, Model model) {
        model.addAttribute("responsavel", buscarPessoaAutenticada(authentication));
        return "html/adicionar_crianca";
    }

    @GetMapping("/html/aluno/{codigo}")
    @Transactional(readOnly = true)
    public String detalheAluno(
            @PathVariable Integer codigo,
            Authentication authentication,
            Model model
    ) {
        PessoaEntity pessoa = buscarPessoaAutenticada(authentication);
        ResponsavelEntity responsavel = buscarResponsavel(pessoa);
        ResponsavelAlunoEntity vinculo = responsavelAlunoRepository.findById(
                        new ResponsavelAlunoId(responsavel.getPessoaCodigo(), codigo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Não foi possível localizar uma criança vinculada à sua conta."));
        AlunoEntity aluno = vinculo.getAluno();

        List<MatriculaEntity> matriculas = matriculaRepository.findByAlunoCodigoOrderByNrDesc(codigo);
        MatriculaEntity matriculaAtiva = matriculas.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 0)
                .findFirst()
                .orElse(null);
        List<DiarioBordoEntity> diarios = new ArrayList<>();
        for (MatriculaEntity matricula : matriculas) {
            diarios.addAll(diarioBordoRepository.findByMatriculaNrOrderByCodigoDesc(matricula.getNr()));
        }
        diarios.sort(Comparator.comparing(DiarioBordoEntity::getCodigo).reversed());

        long diasPresentes = diarios.stream().filter(DiarioBordoEntity::getCompareceu).count();
        long diasAusentes = diarios.size() - diasPresentes;
        int frequencia = diarios.isEmpty() ? 0 : (int) Math.round(diasPresentes * 100.0 / diarios.size());
        String professorNome = matriculaAtiva == null ? null
                : matriculaAtiva.getTurma().getProfessor().getPessoa().getNome();

        model.addAttribute("responsavel", pessoa);
        model.addAttribute("aluno", aluno);
        model.addAttribute("parentesco", vinculo.getGrauParentesco());
        model.addAttribute("matricula", matriculaAtiva);
        model.addAttribute("professorNome", professorNome);
        model.addAttribute("diarios", diarios.stream().limit(10).toList());
        model.addAttribute("diasRegistrados", diarios.size());
        model.addAttribute("diasPresentes", diasPresentes);
        model.addAttribute("diasAusentes", diasAusentes);
        model.addAttribute("frequencia", frequencia);
        model.addAttribute("idade", aluno.getDataNascimento() == null ? null
                : Period.between(aluno.getDataNascimento(), LocalDate.now()).getYears());
        return "html/detalhe_aluno";
    }

    private PessoaEntity buscarPessoaAutenticada(Authentication authentication) {
        return pessoaRepository.findByCpf(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Não foi possível localizar os dados da conta autenticada."));
    }

    private ResponsavelEntity buscarResponsavel(PessoaEntity pessoa) {
        return responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Não foi possível localizar o cadastro de responsável."));
    }

    public record AlunoDashboard(
            AlunoEntity aluno,
            String grauParentesco,
            MatriculaEntity matricula,
            String professorNome,
            DiarioBordoEntity diario
    ) {
    }

    public record ContatoAutorizado(
            String alunoNome,
            String nome,
            String grauParentesco,
            String telefone
    ) {
    }
}
