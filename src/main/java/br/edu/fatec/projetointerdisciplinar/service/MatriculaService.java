package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public MatriculaEntity atualizar(Integer nr, MatriculaEntity matriculaAtualizada) {
        return null;
    }

    public void deletar(Integer nr) {
        matriculaRepository.deleteById(nr);
    }
}
