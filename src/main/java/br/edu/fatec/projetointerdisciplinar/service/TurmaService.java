package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;

    public List<TurmaEntity> listarTodos() {
        return turmaRepository.findAll();
    }

    public Optional<TurmaEntity> buscarPorId(Integer codigo) {
        return turmaRepository.findById(codigo);
    }

    public TurmaEntity salvar(TurmaEntity turma) {
        return turmaRepository.save(turma);
    }

    public TurmaEntity atualizar(Integer codigo, TurmaEntity turmaAtualizada) {
        return null;
    }

    public void deletar(Integer codigo) {
        turmaRepository.deleteById(codigo);
    }
}
