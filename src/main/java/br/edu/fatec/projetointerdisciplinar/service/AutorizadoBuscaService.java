package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutorizadoBuscaService {

    private final AutorizadoBuscaRepository autorizadoBuscaRepository;

    public List<AutorizadoBuscaEntity> listarTodos() {
        return autorizadoBuscaRepository.findAll();
    }

    public Optional<AutorizadoBuscaEntity> buscarPorId(Integer id) {
        return autorizadoBuscaRepository.findById(id);
    }

    public AutorizadoBuscaEntity salvar(AutorizadoBuscaEntity autorizadoBusca) {
        return autorizadoBuscaRepository.save(autorizadoBusca);
    }

    public Optional<AutorizadoBuscaEntity> atualizar(Integer codigo, AutorizadoBuscaEntity autorizadoBuscaAtualizado) {
        return autorizadoBuscaRepository.findById(codigo).map(autorizado -> {
            autorizado.setNome(autorizadoBuscaAtualizado.getNome());
            autorizado.setTelefone(autorizadoBuscaAtualizado.getTelefone());
            autorizado.setGrauParentesco(autorizadoBuscaAtualizado.getGrauParentesco());
            return autorizadoBuscaRepository.save(autorizado);
        });
    }

    public void deletar(Integer id) {
        autorizadoBuscaRepository.deleteById(id);
    }
}
