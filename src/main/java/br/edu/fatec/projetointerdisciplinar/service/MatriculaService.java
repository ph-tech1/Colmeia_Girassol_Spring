package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

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
    public MatriculaEntity atualizarStatus(Integer nr, Integer novoStatus) {
        if (novoStatus == null || novoStatus < 1 || novoStatus > 4) {
            throw new IllegalArgumentException("Informe um status válido para a matrícula.");
        }
        MatriculaEntity matricula = matriculaRepository.findById(nr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada."));
        int statusAtual = matricula.getStatus();
        boolean transicaoPermitida = (statusAtual == 1 && (novoStatus == 2 || novoStatus == 3))
                || (statusAtual == 2 && (novoStatus == 3 || novoStatus == 4));
        if (!transicaoPermitida) {
            throw new IllegalArgumentException("Esta matrícula não pode ser alterada para o status informado.");
        }

        matricula.setStatus(novoStatus);
        matricula.setDataDecisao(LocalDateTime.now());
        return matriculaRepository.save(matricula);
    }

    public MatriculaEntity atualizar(Integer nr, MatriculaEntity matriculaAtualizada) {
        return null;
    }

    public void deletar(Integer nr) {
        matriculaRepository.deleteById(nr);
    }
}
