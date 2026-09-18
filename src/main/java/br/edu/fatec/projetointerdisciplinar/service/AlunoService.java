package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;

    // Buscar todos os alunos
    public List<AlunoEntity> listarTodos() {
        return alunoRepository.findAll();
    }

    // Buscar um aluno por ID
    public Optional<AlunoEntity> buscarPorId(Integer codigo) {
        return alunoRepository.findById(codigo);
    }

    // Salvar/Cadastrar aluno
    public AlunoEntity salvar(AlunoEntity aluno) {
        // TODO: Adicionar validações (ex: verificar se nome não está vazio)
        return alunoRepository.save(aluno);
    }

    // Atualizar aluno
    public AlunoEntity atualizar(Integer codigo, AlunoEntity alunoAtualizado) {
        // TODO: Buscar o aluno existente, atualizar seus campos e salvar
        return null;
    }

    // Excluir aluno
    public void deletar(Integer codigo) {
        // TODO: Verificar se o aluno existe antes de deletar
        alunoRepository.deleteById(codigo);
    }
}