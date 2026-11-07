package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.RegistrarFrequenciaRequest;
import br.edu.fatec.projetointerdisciplinar.model.*;
import br.edu.fatec.projetointerdisciplinar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;
    private final DatabaseProcedureRepository databaseProcedureRepository;
    private final DiarioBordoRepository diarioBordoRepository;
    private final AulaPlanejamentoRepository aulaPlanejamentoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PessoaRepository pessoaRepository;
    private final ProfessorRepository professorRepository;

    @Transactional(readOnly = true)
    public List<FrequenciaEntity> listarTodos() {
        return frequenciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<FrequenciaEntity> buscarPorId(FrequenciaId id) {
        return frequenciaRepository.findById(id);
    }

    @Transactional
    public FrequenciaEntity salvar(RegistrarFrequenciaRequest request, String cpfAutenticado) {
        validarRequest(request);
        AulaPlanejamentoEntity aula = aulaPlanejamentoRepository.findById(request.aulaPlanejamentosCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Aula não encontrada."));
        MatriculaEntity matricula = matriculaRepository.findById(request.matriculaCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));
        ProfessorEntity professor = buscarProfessor(cpfAutenticado);
        if (!professor.getPessoaCodigo().equals(aula.getProfessor().getPessoaCodigo())) {
            throw new IllegalArgumentException("Somente o professor responsável pela aula pode registrar a frequência.");
        }
        if (matricula.getTurma() == null
                || !matricula.getTurma().getCodigo().equals(aula.getTurma().getCodigo())) {
            throw new IllegalArgumentException("A matrícula e a aula devem pertencer à mesma turma.");
        }

        FrequenciaId id = new FrequenciaId(aula.getCodigo(), matricula.getNr());
        if (!"P".equals(request.statusPresenca())
                && diarioBordoRepository.existsById(id)) {
            throw new IllegalArgumentException("Apague o diário de bordo antes de alterar a presença para falta.");
        }

        databaseProcedureRepository.registrarFrequencia(
                aula.getCodigo(),
                matricula.getNr(),
                aula.getTurma().getCodigo(),
                request.statusPresenca()
        );
        return frequenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("A frequência não foi encontrada após o registro."));
    }

    @Transactional
    public void deletar(FrequenciaId id) {
        frequenciaRepository.deleteById(id);
    }

    private void validarRequest(RegistrarFrequenciaRequest request) {
        if (request == null || request.aulaPlanejamentosCodigo() == null || request.matriculaCodigo() == null
                || request.statusPresenca() == null
                || !request.statusPresenca().matches("[PFJ]")) {
            throw new IllegalArgumentException("Informe aula, matrícula e presença válida (P, F ou J).");
        }
    }

    private ProfessorEntity buscarProfessor(String cpf) {
        PessoaEntity pessoa = pessoaRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada não encontrada."));
        ProfessorEntity professor = professorRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("O usuário autenticado não é professor."));
        if (!Integer.valueOf(1).equals(professor.getStatus())) {
            throw new IllegalArgumentException("O cadastro de professor está inativo.");
        }
        return professor;
    }
}
