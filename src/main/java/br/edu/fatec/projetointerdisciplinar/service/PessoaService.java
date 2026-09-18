package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public List<PessoaEntity> listarTodos() {
        return pessoaRepository.findAll();
    }

    public Optional<PessoaEntity> buscarPorId(Integer codigo) {
        return pessoaRepository.findById(codigo);
    }

    public PessoaEntity salvar(PessoaEntity pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public PessoaEntity atualizar(Integer codigo, PessoaEntity pessoaAtualizada) {
        return null;
    }

    public void deletar(Integer codigo) {
        pessoaRepository.deleteById(codigo);
    }
}
