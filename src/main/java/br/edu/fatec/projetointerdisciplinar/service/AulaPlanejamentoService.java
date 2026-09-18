package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.AulaPlanejamentoEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AulaPlanejamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AulaPlanejamentoService {

    private final AulaPlanejamentoRepository aulaPlanejamentoRepository;

    public List<AulaPlanejamentoEntity> listarTodos() {
        return aulaPlanejamentoRepository.findAll();
    }

    public Optional<AulaPlanejamentoEntity> buscarPorId(Integer codigo) {
        return aulaPlanejamentoRepository.findById(codigo);
    }

    public AulaPlanejamentoEntity salvar(AulaPlanejamentoEntity aulaPlanejamento) {
        return aulaPlanejamentoRepository.save(aulaPlanejamento);
    }

    public AulaPlanejamentoEntity atualizar(Integer codigo, AulaPlanejamentoEntity aulaPlanejamentoAtualizado) {
        return null;
    }

    public void deletar(Integer codigo) {
        aulaPlanejamentoRepository.deleteById(codigo);
    }
}
