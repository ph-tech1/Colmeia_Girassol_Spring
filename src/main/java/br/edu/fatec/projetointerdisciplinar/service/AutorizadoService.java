package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.AtualizarAutorizadoRequest;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutorizadoService {

    private final AutorizadoRepository autorizadoRepository;

    @Transactional
    public void atualizar(Integer codigo, AtualizarAutorizadoRequest request) {
        if (request == null || request.nome() == null || request.nome().isBlank()
                || request.telefone() == null || request.telefone().isBlank()) {
            throw new IllegalArgumentException("Informe o nome e o telefone do autorizado.");
        }

        String nome = request.nome().trim();
        String rg = request.rg() == null || request.rg().isBlank()
                ? null
                : digitos(request.rg());
        String telefone = digitos(request.telefone());
        if (nome.length() < 3 || nome.length() > 100
                || (rg != null && rg.length() > 20)
                || telefone.length() < 10 || telefone.length() > 15) {
            throw new IllegalArgumentException("Confira o nome, RG e telefone informados.");
        }

        AutorizadoEntity autorizado = autorizadoRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Pessoa autorizada não encontrada."));
        PessoaEntity pessoa = autorizado.getPessoa();
        if (pessoa == null) {
            autorizado.setNome(nome);
            autorizado.setRg(rg);
            autorizado.setTelefone(telefone);
        } else {
            pessoa.setNome(nome);
            pessoa.setRg(rg);
            pessoa.setTelefone(telefone);
            autorizado.setNome(null);
            autorizado.setRg(null);
            autorizado.setTelefone(null);
        }
        autorizadoRepository.save(autorizado);
    }

    private String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
