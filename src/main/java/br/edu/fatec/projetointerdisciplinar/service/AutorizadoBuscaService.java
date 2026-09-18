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

    public Optional<AutorizadoBuscaEntity> buscarPorId(Integer codigo) {
        return autorizadoBuscaRepository.findById(codigo);
    }

    public AutorizadoBuscaEntity salvar(AutorizadoBuscaEntity autorizadoBusca) {
        return autorizadoBuscaRepository.save(autorizadoBusca);
    }

    public AutorizadoBuscaEntity atualizar(Integer codigo, AutorizadoBuscaEntity autorizadoBuscaAtualizado) {
        return null;
    }

    public void deletar(Integer codigo) {
        autorizadoBuscaRepository.deleteById(codigo);
    }
}
