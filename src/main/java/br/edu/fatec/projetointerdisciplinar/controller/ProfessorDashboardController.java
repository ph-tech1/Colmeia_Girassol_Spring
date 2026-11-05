package br.edu.fatec.projetointerdisciplinar.controller;

import br.edu.fatec.projetointerdisciplinar.model.AulaPlanejamentoEntity;
import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaId;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AulaPlanejamentoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.DiarioBordoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.FrequenciaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelAlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/professor/dashboard")
@RequiredArgsConstructor
public class ProfessorDashboardController {

    private final PessoaRepository pessoaRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final AulaPlanejamentoRepository aulaPlanejamentoRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final DiarioBordoRepository diarioBordoRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public DashboardResponse carregar(Authentication authentication) {
        ProfessorEntity professor = buscarProfessor(authentication);
        List<TurmaEntity> turmas = turmaRepository.findByProfessorPessoaCodigo(professor.getPessoaCodigo());
        List<TurmaResumo> turmasResumo = new ArrayList<>();
        List<PlanejamentoResumo> planejamentos = new ArrayList<>();
        List<FrequenciaResumo> frequencias = new ArrayList<>();
        List<FrequenciaId> frequenciaIds = new ArrayList<>();

        for (TurmaEntity turma : turmas) {
            List<AlunoResumo> alunos = matriculaRepository.findByTurmaCodigo(turma.getCodigo()).stream()
                    .filter(matricula -> Integer.valueOf(2).equals(matricula.getStatus()))
                    .filter(matricula -> matricula.getAluno() != null)
                    .map(matricula -> {
                        var responsavel = responsavelAlunoRepository.findByIdAlunoCodigo(matricula.getAluno().getCodigo())
                                .stream()
                                .sorted(Comparator.comparingInt(item -> item.getOrdemContato() == null ? 999 : item.getOrdemContato()))
                                .findFirst()
                                .orElse(null);

                        String nomeResponsavel = responsavel != null && responsavel.getResponsavel() != null
                                && responsavel.getResponsavel().getPessoa() != null
                                ? responsavel.getResponsavel().getPessoa().getNome() : null;
                        String telefoneResponsavel = responsavel != null && responsavel.getResponsavel() != null
                                && responsavel.getResponsavel().getPessoa() != null
                                ? responsavel.getResponsavel().getPessoa().getTelefone() : null;

                        return new AlunoResumo(
                                matricula.getNr(),
                                matricula.getAluno().getCodigo(),
                                matricula.getAluno().getNome(),
                                nomeResponsavel,
                                telefoneResponsavel);
                    })
                    .sorted(Comparator.comparing(AlunoResumo::nome, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
            turmasResumo.add(new TurmaResumo(
                    turma.getCodigo(), turma.getNomeTurma(), turma.getAno(), turma.getGrau(), alunos));

            for (AulaPlanejamentoEntity aula : aulaPlanejamentoRepository.findByTurmaCodigo(turma.getCodigo())) {
                if (!professor.getPessoaCodigo().equals(aula.getProfessor().getPessoaCodigo())) {
                    continue;
                }
                PlanejamentoResumo planejamento = new PlanejamentoResumo(
                        aula.getCodigo(), turma.getCodigo(), turma.getNomeTurma(), aula.getDataAula(),
                        aula.getAtividadeDinamica(), aula.getDescricao(), aula.getStatus());
                planejamentos.add(planejamento);
                for (FrequenciaEntity frequencia : frequenciaRepository
                        .findByIdAulaPlanejamentosCodigo(aula.getCodigo())) {
                    frequencias.add(new FrequenciaResumo(
                            aula.getCodigo(), frequencia.getId().getMatriculaCodigo(),
                            frequencia.getStatusPresenca()));
                    frequenciaIds.add(frequencia.getId());
                }
            }
        }

        List<DiarioResumo> diarios = diarioBordoRepository.findAllById(frequenciaIds).stream()
                .map(this::paraDiarioResumo)
                .toList();
        planejamentos.sort(Comparator.comparing(PlanejamentoResumo::dataAula).reversed());
        return new DashboardResponse(
                professor.getPessoa().getNome(),
                professor.getFormacao(),
                turmasResumo,
                planejamentos,
                frequencias,
                diarios);
    }

    @PostMapping("/planejamentos")
    @Transactional
    public ResponseEntity<?> criarPlanejamento(
            @RequestBody PlanejamentoRequest request,
            Authentication authentication
    ) {
        ProfessorEntity professor = buscarProfessor(authentication);
        validarRequisicaoPlanejamento(request);
        TurmaEntity turma = buscarTurmaDoProfessor(request.turmaCodigo(), professor);
        if (aulaPlanejamentoRepository.existsByTurmaCodigoAndDataAula(turma.getCodigo(), request.dataAula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um planejamento para essa turma nesta data.");
        }

        AulaPlanejamentoEntity aula = new AulaPlanejamentoEntity();
        aula.setProfessor(professor);
        aula.setTurma(turma);
        aula.setDataAula(request.dataAula());
        aula.setAtividadeDinamica(request.atividadeDinamica().trim());
        aula.setDescricao(request.descricao().trim());
        aula.setStatus(1);
        AulaPlanejamentoEntity salva = aulaPlanejamentoRepository.save(aula);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumo(salva));
    }

    @PutMapping("/planejamentos/{codigo}")
    @Transactional
    public PlanejamentoResumo atualizarPlanejamento(
            @PathVariable Integer codigo,
            @RequestBody PlanejamentoRequest request,
            Authentication authentication
    ) {
        ProfessorEntity professor = buscarProfessor(authentication);
        validarRequisicaoPlanejamento(request);
        AulaPlanejamentoEntity aula = aulaPlanejamentoRepository.findById(codigo)
                .filter(item -> professor.getPessoaCodigo().equals(item.getProfessor().getPessoaCodigo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aula planejada não encontrada."));
        if (!aula.getTurma().getCodigo().equals(request.turmaCodigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A turma de uma aula existente não pode ser alterada.");
        }
        TurmaEntity turma = aula.getTurma();
        if (aulaPlanejamentoRepository.existsByTurmaCodigoAndDataAulaAndCodigoNot(
                turma.getCodigo(), request.dataAula(), codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um planejamento para essa turma nesta data.");
        }

        aula.setDataAula(request.dataAula());
        aula.setAtividadeDinamica(request.atividadeDinamica().trim());
        aula.setDescricao(request.descricao().trim());
        return resumo(aulaPlanejamentoRepository.save(aula));
    }

    private void validarRequisicaoPlanejamento(PlanejamentoRequest request) {
        if (request == null || request.turmaCodigo() == null || request.dataAula() == null
                || request.atividadeDinamica() == null || request.descricao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha os dados do planejamento.");
        }
        String titulo = request.atividadeDinamica().trim();
        String descricao = request.descricao().trim();
        if (titulo.length() < 3 || titulo.length() > 200 || descricao.length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um título entre 3 e 200 caracteres e uma descrição com pelo menos 5 caracteres.");
        }
    }

    private TurmaEntity buscarTurmaDoProfessor(Integer turmaCodigo, ProfessorEntity professor) {
        TurmaEntity turma = turmaRepository.findById(turmaCodigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
        if (!professor.getPessoaCodigo().equals(turma.getProfessor().getPessoaCodigo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Só é possível planejar aulas para uma turma vinculada à sua conta.");
        }
        return turma;
    }

    private PlanejamentoResumo resumo(AulaPlanejamentoEntity aula) {
        return new PlanejamentoResumo(
                aula.getCodigo(), aula.getTurma().getCodigo(), aula.getTurma().getNomeTurma(), aula.getDataAula(),
                aula.getAtividadeDinamica(), aula.getDescricao(), aula.getStatus());
    }

    private ProfessorEntity buscarProfessor(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar o painel.");
        }
        PessoaEntity pessoa = pessoaRepository.findByCpf(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A conta autenticada não está vinculada a um professor."));
        ProfessorEntity professor = professorRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A conta autenticada não está vinculada a um professor."));
        if (!Integer.valueOf(1).equals(professor.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O cadastro de professor está inativo.");
        }
        return professor;
    }

    private DiarioResumo paraDiarioResumo(DiarioBordoEntity diario) {
        return new DiarioResumo(
                diario.getId().getAulaPlanejamentosCodigo(),
                diario.getId().getMatriculaCodigo(),
                diario.getAlimentacao(),
                diario.getSono(),
                diario.getHumor(),
                diario.getBanheiroFralda(),
                diario.getObservacoes());
    }

    public record DashboardResponse(
            String nome,
            String formacao,
            List<TurmaResumo> turmas,
            List<PlanejamentoResumo> planejamentos,
            List<FrequenciaResumo> frequencias,
            List<DiarioResumo> diarios
    ) {
    }

    public record TurmaResumo(
            Integer codigo,
            String nomeTurma,
            Integer ano,
            String grau,
            List<AlunoResumo> alunos
    ) {
    }

    public record AlunoResumo(Integer matriculaCodigo, Integer codigo, String nome,
                             String nomeResponsavel, String telefoneResponsavel) {
    }

    public record PlanejamentoResumo(
            Integer codigo,
            Integer turmaCodigo,
            String turmaNome,
            java.time.LocalDate dataAula,
            String atividadeDinamica,
            String descricao,
            Integer status
    ) {
    }

    public record FrequenciaResumo(Integer aulaCodigo, Integer matriculaCodigo, String statusPresenca) {
    }

    public record DiarioResumo(
            Integer aulaCodigo,
            Integer matriculaCodigo,
            String alimentacao,
            String sono,
            String humor,
            String banheiroFralda,
            String observacoes
    ) {
    }

    public record PlanejamentoRequest(
            Integer turmaCodigo,
            java.time.LocalDate dataAula,
            String atividadeDinamica,
            String descricao
    ) {
    }
}
