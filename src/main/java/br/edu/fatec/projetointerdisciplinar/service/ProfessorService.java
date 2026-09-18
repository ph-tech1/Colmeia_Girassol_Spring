package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public List<ProfessorEntity> listarTodos() {
        return professorRepository.findAll();
    }

    public Optional<ProfessorEntity> buscarPorId(Integer pessoaCodigo) {
        return professorRepository.findById(pessoaCodigo);
    }

    public ProfessorEntity salvar(ProfessorEntity professor) {
        return professorRepository.save(professor);
    }

    public ProfessorEntity atualizar(Integer pessoaCodigo, ProfessorEntity professorAtualizado) {
        return null;
    }

    public void deletar(Integer pessoaCodigo) {
        professorRepository.deleteById(pessoaCodigo);
    }
}
