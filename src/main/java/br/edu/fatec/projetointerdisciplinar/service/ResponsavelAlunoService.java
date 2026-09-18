package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelAlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponsavelAlunoService {

    private final ResponsavelAlunoRepository responsavelAlunoRepository;

    public List<ResponsavelAlunoEntity> listarTodos() {
        return responsavelAlunoRepository.findAll();
    }

    public Optional<ResponsavelAlunoEntity> buscarPorId(ResponsavelAlunoId id) {
        return responsavelAlunoRepository.findById(id);
    }

    public ResponsavelAlunoEntity salvar(ResponsavelAlunoEntity responsavelAluno) {
        return responsavelAlunoRepository.save(responsavelAluno);
    }

    public ResponsavelAlunoEntity atualizar(ResponsavelAlunoId id, ResponsavelAlunoEntity responsavelAlunoAtualizado) {
        return null;
    }

    public void deletar(ResponsavelAlunoId id) {
        responsavelAlunoRepository.deleteById(id);
    }
}
