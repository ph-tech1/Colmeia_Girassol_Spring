package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CpfUserDetailsServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private ResponsavelRepository responsavelRepository;

    @InjectMocks
    private CpfUserDetailsService userDetailsService;

    @Test
    void loadsActiveProfessorUsingCpfWithFormatting() {
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setCodigo(7);
        pessoa.setCpf("12345678901");
        pessoa.setSenha("$2a$12$encoded-password");

        ProfessorEntity professor = new ProfessorEntity();
        professor.setStatus(1);

        when(pessoaRepository.findByCpf("12345678901")).thenReturn(Optional.of(pessoa));
        when(professorRepository.findById(7)).thenReturn(Optional.of(professor));
        when(responsavelRepository.findById(7)).thenReturn(Optional.empty());

        UserDetails user = userDetailsService.loadUserByUsername("123.456.789-01");

        assertEquals("12345678901", user.getUsername());
        assertEquals("ROLE_PROFESSOR", user.getAuthorities().iterator().next().getAuthority());
        verify(pessoaRepository).findByCpf("12345678901");
    }

    @Test
    void loadsTestAccountUsingShortRepeatedCpf() {
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setCodigo(9);
        pessoa.setCpf("11111111");
        pessoa.setSenha("$2a$12$encoded-password");

        ResponsavelEntity responsavel = new ResponsavelEntity();
        responsavel.setStatus(1);

        when(pessoaRepository.findByCpf("11111111")).thenReturn(Optional.of(pessoa));
        when(professorRepository.findById(9)).thenReturn(Optional.empty());
        when(responsavelRepository.findById(9)).thenReturn(Optional.of(responsavel));

        UserDetails user = userDetailsService.loadUserByUsername("11111111");

        assertEquals("11111111", user.getUsername());
        assertEquals("ROLE_RESPONSAVEL", user.getAuthorities().iterator().next().getAuthority());
        verify(pessoaRepository).findByCpf("11111111");
    }

    @Test
    void rejectsPendingResponsibleAccount() {
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setCodigo(8);
        pessoa.setSenha("$2a$12$encoded-password");

        ResponsavelEntity responsavel = new ResponsavelEntity();
        responsavel.setStatus(0);

        when(pessoaRepository.findByCpf("10987654321")).thenReturn(Optional.of(pessoa));
        when(professorRepository.findById(8)).thenReturn(Optional.empty());
        when(responsavelRepository.findById(8)).thenReturn(Optional.of(responsavel));

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("10987654321"));
    }

    @Test
    void rejectsUsernameThatIsNotACpf() {
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("admin"));
    }
}
