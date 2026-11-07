package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.DatabaseProcedureRepository;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final TurmaRepository turmaRepository;
    private final DatabaseProcedureRepository databaseProcedureRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<MatriculaEntity> listarTodos() {
        return matriculaRepository.findAll();
    }

    public Optional<MatriculaEntity> buscarPorId(Integer nr) {
        return matriculaRepository.findById(nr);
    }

    public MatriculaEntity salvar(MatriculaEntity matricula) {
        return matriculaRepository.save(matricula);
    }

    @Transactional
    public MatriculaEntity atualizarStatus(Integer nr, Integer novoStatus, String motivoRejeicao) {
        if (novoStatus == null || novoStatus < 1 || novoStatus > 3) {
            throw new IllegalArgumentException("Informe um status válido para a matrícula.");
        }
        String motivo = motivoRejeicao == null ? null : motivoRejeicao.trim();
        if (novoStatus == 3 && (motivo == null || motivo.isEmpty())) {
            throw new IllegalArgumentException("Informe o motivo da rejeição.");
        }
        if (motivo != null && motivo.length() > 500) {
            throw new IllegalArgumentException("O motivo da rejeição deve ter no máximo 500 caracteres.");
        }

        MatriculaEntity matricula = matriculaRepository.findById(nr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada."));
        Integer statusAtual = matricula.getStatus();
        boolean transicaoPermitida = statusAtual != null
                && ((statusAtual == 0 && novoStatus == 1)
                || (statusAtual == 1 && (novoStatus == 2 || novoStatus == 3)));
        if (!transicaoPermitida) {
            throw new IllegalArgumentException("Esta matrícula não pode ser alterada para o status informado.");
        }

        databaseProcedureRepository.decidirMatricula(nr, novoStatus, novoStatus == 3 ? motivo : null);
        entityManager.refresh(matricula);
        return matricula;
    }

    @Transactional
    public MatriculaEntity atribuirTurma(Integer nr, Integer turmaCodigo) {
        if (turmaCodigo == null) {
            throw new IllegalArgumentException("Selecione uma turma.");
        }

        MatriculaEntity matricula = matriculaRepository.findById(nr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada."));
        if (!Integer.valueOf(2).equals(matricula.getStatus())) {
            throw new IllegalArgumentException("Só é possível atribuir turma a uma matrícula aprovada.");
        }

        turmaRepository.findById(turmaCodigo)
                .orElseThrow(() -> new IllegalArgumentException("A turma selecionada não foi encontrada."));
        databaseProcedureRepository.atribuirTurma(nr, turmaCodigo);
        entityManager.refresh(matricula);
        return matricula;
    }

    public MatriculaEntity atualizar(Integer nr, MatriculaEntity matriculaAtualizada) {
        return null;
    }

    public void deletar(Integer nr) {
        matriculaRepository.deleteById(nr);
    }
}
