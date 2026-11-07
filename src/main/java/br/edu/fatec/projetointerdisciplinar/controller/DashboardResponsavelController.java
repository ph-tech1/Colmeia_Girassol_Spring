package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.DatabaseFunctionRepository;
import br.edu.fatec.projetointerdisciplinar.repository.DatabaseViewRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardResponsavelController {

    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final DatabaseFunctionRepository databaseFunctionRepository;
    private final DatabaseViewRepository databaseViewRepository;
    private final AutorizadoBuscaRepository autorizadoBuscaRepository;
    private final ParentescoRepository parentescoRepository;

    @GetMapping("/html/dashboard_responsavel.html")
    @Transactional(readOnly = true)
    public String dashboard(Authentication authentication, Model model) {
        PessoaEntity pessoa = buscarPessoaAutenticada(authentication);
        ResponsavelEntity responsavel = buscarResponsavel(pessoa);

        List<AlunoDashboard> alunos = new ArrayList<>();
        List<ContatoAutorizado> autorizados = new ArrayList<>();
        Map<Integer, DatabaseViewRepository.ResumoFrequencia> frequenciasPorAluno =
                databaseViewRepository.listarResumoFrequenciaPorResponsavel(responsavel.getPessoaCodigo())
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(
                                DatabaseViewRepository.ResumoFrequencia::alunoCodigo,
                                resumo -> resumo,
                                (first, ignored) -> first));
        Map<Integer, DatabaseViewRepository.DiarioAluno> diarioMaisRecentePorMatricula = new HashMap<>();
        for (DatabaseViewRepository.DiarioAluno diario : databaseViewRepository
                .listarDiarioPorResponsavel(responsavel.getPessoaCodigo())) {
            diarioMaisRecentePorMatricula.putIfAbsent(diario.matriculaCodigo(), diario);
        }

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
            DatabaseViewRepository.DiarioAluno diarioAtual = matriculaAtiva
                    ? diarioMaisRecentePorMatricula.get(matricula.getNr()) : null;
            RegistroDiario diario = diarioAtual == null ? null : paraRegistroDiario(diarioAtual);

            String professorNome = matriculaAtiva
                    ? matricula.getTurma().getProfessor().getPessoa().getNome()
                    : null;
            alunos.add(new AlunoDashboard(
                    aluno,
                    vinculo.getParentesco().getDescricao(),
                    matricula,
                    professorNome,
                    frequenciasPorAluno.get(aluno.getCodigo()),
                    diario));

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
        MatriculaEntity matriculaParaRelatorio = matriculaAtiva != null
                ? matriculaAtiva
                : matriculas.stream().findFirst().orElse(null);
        DatabaseFunctionRepository.ResumoFrequencia resumoFrequencia = matriculaParaRelatorio == null
                ? null
                : databaseFunctionRepository.buscarResumoFrequencia(matriculaParaRelatorio.getNr())
                .orElseThrow(() -> new IllegalStateException(
                        "A matrícula selecionada não foi encontrada pela função de resumo de frequência."));
        List<RegistroDiario> registrosDiario = matriculaParaRelatorio == null
                ? List.of()
                : databaseFunctionRepository.listarHistoricoFrequencia(matriculaParaRelatorio.getNr())
                .stream()
                .map(this::paraRegistroDiario)
                .toList();
        int diasRegistrados = resumoFrequencia == null ? 0 : Math.toIntExact(resumoFrequencia.diasRegistrados());
        int diasPresentes = resumoFrequencia == null ? 0 : Math.toIntExact(resumoFrequencia.presencas());
        int diasAusentes = resumoFrequencia == null ? 0
                : Math.toIntExact(resumoFrequencia.faltas() + resumoFrequencia.faltasJustificadas());
        int frequencia = matriculaParaRelatorio == null ? 0
                : databaseFunctionRepository.buscarPercentualPresenca(matriculaParaRelatorio.getNr())
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .intValue();
        String professorNome = matriculaAtiva == null || matriculaAtiva.getTurma() == null
                ? null
                : matriculaAtiva.getTurma().getProfessor().getPessoa().getNome();

        model.addAttribute("responsavel", pessoa);
        model.addAttribute("aluno", aluno);
        model.addAttribute("parentesco", vinculo.getParentesco().getDescricao());
        model.addAttribute("matricula", matriculaAtiva);
        model.addAttribute("professorNome", professorNome);
        model.addAttribute("diarios", registrosDiario.stream().limit(10).toList());
        model.addAttribute("diasRegistrados", diasRegistrados);
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

    private RegistroDiario paraRegistroDiario(DatabaseViewRepository.DiarioAluno diario) {
        return new RegistroDiario(
                diario.dataAula(),
                "P".equals(diario.statusPresenca()),
                diario.statusPresenca(),
                diario.humor(),
                diario.alimentacao(),
                diario.sono(),
                diario.banheiroFralda(),
                diario.observacoes(),
                diario.nomeTurma(),
                diario.atividadeDinamica(),
                diario.professorNome()
        );
    }

    private RegistroDiario paraRegistroDiario(DatabaseFunctionRepository.RegistroFrequencia frequencia) {
        return new RegistroDiario(
                frequencia.dataAula(),
                "P".equals(frequencia.statusPresenca()),
                frequencia.statusPresenca(),
                frequencia.humor(),
                frequencia.alimentacao(),
                frequencia.sono(),
                frequencia.banheiroFralda(),
                frequencia.observacoes(),
                frequencia.turmaNome(),
                frequencia.atividadeDinamica(),
                frequencia.professorNome()
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
            String observacoes,
            String turmaNome,
            String atividadeDinamica,
            String professorNome
    ) {
    }

    public record AlunoDashboard(
            AlunoEntity aluno,
            String grauParentesco,
            MatriculaEntity matricula,
            String professorNome,
            DatabaseViewRepository.ResumoFrequencia frequencia,
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
