package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final MatriculaRepository matriculaRepository;

    public List<TurmaEntity> listarTodos() {
        return turmaRepository.findAll();
    }

    public Optional<TurmaEntity> buscarPorId(Integer codigo) {
        return turmaRepository.findById(codigo);
    }

    @Transactional
    public TurmaEntity salvar(TurmaEntity turma) {
        validarEAplicar(turma, turma);
        return turmaRepository.save(turma);
    }

    @Transactional
    public TurmaEntity atualizar(Integer codigo, TurmaEntity turmaAtualizada) {
        TurmaEntity turma = turmaRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("A turma selecionada não foi encontrada."));
        validarEAplicar(turmaAtualizada, turma);
        return turmaRepository.save(turma);
    }

    @Transactional
    public void deletar(Integer codigo) {
        TurmaEntity turma = turmaRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("A turma selecionada não foi encontrada."));
        List<MatriculaEntity> matriculas = matriculaRepository.findByTurmaCodigo(codigo);
        matriculas.forEach(matricula -> matricula.setTurma(null));
        if (!matriculas.isEmpty()) {
            matriculaRepository.saveAll(matriculas);
        }
        turmaRepository.delete(turma);
    }

    private void validarEAplicar(TurmaEntity dados, TurmaEntity destino) {
        if (dados == null || dados.getNomeTurma() == null || dados.getNomeTurma().isBlank()
                || dados.getNomeTurma().trim().length() > 50
                || dados.getAno() == null || dados.getGrau() == null || dados.getGrau().isBlank()
                || dados.getGrau().trim().length() > 50) {
            throw new IllegalArgumentException("Preencha nome, ano e etapa da turma com valores válidos.");
        }

        if (dados.getProfessor() == null || dados.getProfessor().getPessoaCodigo() == null) {
            throw new IllegalArgumentException("Selecione um professor para a turma.");
        }
        ProfessorEntity professor = professorRepository.findById(dados.getProfessor().getPessoaCodigo())
                .orElseThrow(() -> new IllegalArgumentException("O professor selecionado não foi encontrado."));
        if (!Integer.valueOf(1).equals(professor.getStatus())) {
            throw new IllegalArgumentException("Selecione um professor ativo.");
        }

        destino.setNomeTurma(dados.getNomeTurma().trim());
        destino.setAno(dados.getAno());
        destino.setGrau(dados.getGrau().trim());
        destino.setProfessor(professor);
    }
}
