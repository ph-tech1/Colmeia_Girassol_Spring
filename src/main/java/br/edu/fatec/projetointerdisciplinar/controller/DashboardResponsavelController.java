package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.DiarioBordoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.FrequenciaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ParentescoRepository;
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
    private final FrequenciaRepository frequenciaRepository;
    private final AutorizadoBuscaRepository autorizadoBuscaRepository;
    private final ParentescoRepository parentescoRepository;

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
                    .findFirst()
                    .orElse(null);

            boolean matriculaAtiva = matricula != null
                    && matricula.getStatus() == 2
                    && matricula.getTurma() != null;
            RegistroDiario diario = !matriculaAtiva ? null
                    : frequenciaRepository.findByMatricula_NrOrderByAulaPlanejamento_DataAulaDesc(matricula.getNr())
                    .stream()
                    .findFirst()
                    .map(this::paraRegistroDiario)
                    .orElse(null);

            String professorNome = matriculaAtiva
                    ? matricula.getTurma().getProfessor().getPessoa().getNome()
                    : null;
            alunos.add(new AlunoDashboard(
                    aluno, vinculo.getParentesco().getDescricao(), matricula, professorNome, diario));

            for (AutorizadoBuscaEntity autorizado : autorizadoBuscaRepository
                    .findByVinculo_Aluno_CodigoAndAtivoTrue(aluno.getCodigo())) {
                autorizados.add(new ContatoAutorizado(
                        autorizado.getCodigo(),
                        aluno.getNome(),
                        autorizado.getAutorizado().getNome(),
                        autorizado.getParentesco().getDescricao(),
                        autorizado.getAutorizado().getTelefone()
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
        model.addAttribute("parentescos", parentescoRepository.findAll());
        return "html/adicionar_crianca";
    }

    @GetMapping("/html/adicionar-autorizado")
    @Transactional(readOnly = true)
    public String adicionarAutorizado(Authentication authentication, Model model) {
        PessoaEntity pessoa = buscarPessoaAutenticada(authentication);
        ResponsavelEntity responsavel = buscarResponsavel(pessoa);
        model.addAttribute("vinculos",
                responsavelAlunoRepository.findByIdResponsavelCodigo(responsavel.getPessoaCodigo()));
        model.addAttribute("parentescos", parentescoRepository.findAll());
        return "html/adicionar_autorizado";
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
                .filter(item -> item.getStatus() != null && item.getStatus() == 2)
                .findFirst()
                .orElse(null);
        List<FrequenciaEntity> registrosFrequencia = new ArrayList<>();
        for (MatriculaEntity matricula : matriculas) {
            registrosFrequencia.addAll(frequenciaRepository.findByMatricula_Nr(matricula.getNr()));
        }
        registrosFrequencia.sort(Comparator.comparing(
                (FrequenciaEntity registro) -> registro.getAulaPlanejamento().getDataAula()).reversed());

        long diasPresentes = registrosFrequencia.stream()
                .filter(registro -> "P".equals(registro.getStatusPresenca())).count();
        long diasAusentes = registrosFrequencia.size() - diasPresentes;
        int frequencia = registrosFrequencia.isEmpty() ? 0
                : (int) Math.round(diasPresentes * 100.0 / registrosFrequencia.size());
        String professorNome = matriculaAtiva == null || matriculaAtiva.getTurma() == null
                ? null
                : matriculaAtiva.getTurma().getProfessor().getPessoa().getNome();

        model.addAttribute("responsavel", pessoa);
        model.addAttribute("aluno", aluno);
        model.addAttribute("parentesco", vinculo.getParentesco().getDescricao());
        model.addAttribute("matricula", matriculaAtiva);
        model.addAttribute("professorNome", professorNome);
        model.addAttribute("diarios", registrosFrequencia.stream().limit(10)
                .map(this::paraRegistroDiario).toList());
        model.addAttribute("diasRegistrados", registrosFrequencia.size());
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

    private RegistroDiario paraRegistroDiario(FrequenciaEntity frequencia) {
        DiarioBordoEntity diario = "P".equals(frequencia.getStatusPresenca())
                ? diarioBordoRepository.findById(frequencia.getId()).orElse(null)
                : null;
        return new RegistroDiario(
                frequencia.getAulaPlanejamento().getDataAula(),
                "P".equals(frequencia.getStatusPresenca()),
                frequencia.getStatusPresenca(),
                diario == null ? null : diario.getHumor(),
                diario == null ? null : diario.getAlimentacao(),
                diario == null ? null : diario.getSono(),
                diario == null ? null : diario.getBanheiroFralda(),
                diario == null ? null : diario.getObservacoes()
        );
    }

    public record RegistroDiario(
            LocalDate dataRegistro,
            Boolean compareceu,
            String statusPresenca,
            String humor,
            String alimentacao,
            String sono,
            String banheiroFralda,
            String observacoes
    ) {
    }

    public record AlunoDashboard(
            AlunoEntity aluno,
            String grauParentesco,
            MatriculaEntity matricula,
            String professorNome,
            RegistroDiario diario
    ) {
    }

    public record ContatoAutorizado(
            Integer codigo,
            String alunoNome,
            String nome,
            String grauParentesco,
            String telefone
    ) {
    }
}
