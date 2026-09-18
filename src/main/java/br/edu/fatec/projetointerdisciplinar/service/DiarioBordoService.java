package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.repository.DiarioBordoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiarioBordoService {

    private final DiarioBordoRepository diarioBordoRepository;

    public List<DiarioBordoEntity> listarTodos() {
        return diarioBordoRepository.findAll();
    }

    public Optional<DiarioBordoEntity> buscarPorId(Integer codigo) {
        return diarioBordoRepository.findById(codigo);
    }

    public DiarioBordoEntity salvar(DiarioBordoEntity diarioBordo) {
        return diarioBordoRepository.save(diarioBordo);
    }

    public DiarioBordoEntity atualizar(Integer codigo, DiarioBordoEntity diarioBordoAtualizado) {
        return null;
    }

    public void deletar(Integer codigo) {
        diarioBordoRepository.deleteById(codigo);
    }
}
