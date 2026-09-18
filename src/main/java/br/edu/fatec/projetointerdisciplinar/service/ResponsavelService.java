package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;

    public List<ResponsavelEntity> listarTodos() {
        return responsavelRepository.findAll();
    }

    public Optional<ResponsavelEntity> buscarPorId(Integer pessoaCodigo) {
        return responsavelRepository.findById(pessoaCodigo);
    }

    public ResponsavelEntity salvar(ResponsavelEntity responsavel) {
        return responsavelRepository.save(responsavel);
    }

    public ResponsavelEntity atualizar(Integer pessoaCodigo, ResponsavelEntity responsavelAtualizado) {
        return null;
    }

    public void deletar(Integer pessoaCodigo) {
        responsavelRepository.deleteById(pessoaCodigo);
    }
}
