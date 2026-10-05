package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CpfUserDetailsService implements UserDetailsService {

    private final PessoaRepository pessoaRepository;
    private final ProfessorRepository professorRepository;
    private final ResponsavelRepository responsavelRepository;

    @Override
    public UserDetails loadUserByUsername(String cpfInformado) throws UsernameNotFoundException {
        String cpf = cpfInformado == null ? "" : cpfInformado.replaceAll("\\D", "");
        if (cpf.length() != 11) {
            throw new UsernameNotFoundException("Credenciais inválidas.");
        }

        PessoaEntity pessoa = pessoaRepository.findByCpf(cpf)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));

        List<String> perfis = new ArrayList<>();
        professorRepository.findById(pessoa.getCodigo())
                .map(ProfessorEntity::getStatus)
                .filter(status -> Objects.equals(status, 1))
                .ifPresent(status -> perfis.add("PROFESSOR"));
        responsavelRepository.findById(pessoa.getCodigo())
                .map(ResponsavelEntity::getStatus)
                .filter(status -> Objects.equals(status, 1))
                .ifPresent(status -> perfis.add("RESPONSAVEL"));

        if (perfis.isEmpty() || pessoa.getSenha() == null || pessoa.getSenha().isBlank()) {
            throw new UsernameNotFoundException("Credenciais inválidas.");
        }

        return User.withUsername(cpf)
                .password(pessoa.getSenha())
                .roles(perfis.toArray(String[]::new))
                .build();
    }
}
