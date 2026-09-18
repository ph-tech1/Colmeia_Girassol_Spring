package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaId;
import br.edu.fatec.projetointerdisciplinar.repository.FrequenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;

    public List<FrequenciaEntity> listarTodos() {
        return frequenciaRepository.findAll();
    }

    public Optional<FrequenciaEntity> buscarPorId(FrequenciaId id) {
        return frequenciaRepository.findById(id);
    }

    public FrequenciaEntity salvar(FrequenciaEntity frequencia) {
        return frequenciaRepository.save(frequencia);
    }

    public FrequenciaEntity atualizar(FrequenciaId id, FrequenciaEntity frequenciaAtualizada) {
        return null;
    }

    public void deletar(FrequenciaId id) {
        frequenciaRepository.deleteById(id);
    }
}
