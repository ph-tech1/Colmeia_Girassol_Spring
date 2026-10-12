package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.NovoProfessorRequest;
import br.edu.fatec.projetointerdisciplinar.dto.ProfessorAdminResponse;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProfessorService {

    private final PessoaRepository pessoaRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ProfessorAdminResponse> listar() {
        return professorRepository.findAll().stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public ProfessorAdminResponse cadastrar(NovoProfessorRequest request) {
        validar(request);

        String cpf = digitos(request.cpf());
        String email = request.email().trim();
        String rg = request.rg() == null || request.rg().isBlank()
                ? null
                : digitos(request.rg());

        if (pessoaRepository.findByCpf(cpf).isPresent()) {
            throw new IllegalStateException("Já existe uma pessoa cadastrada com este CPF.");
        }
        if (pessoaRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Já existe uma pessoa cadastrada com este e-mail.");
        }
        if (rg != null && pessoaRepository.findByRg(rg).isPresent()) {
            throw new IllegalStateException("Já existe uma pessoa cadastrada com este RG.");
        }

        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setNome(request.nome().trim());
        pessoa.setEmail(email);
        pessoa.setCpf(cpf);
        pessoa.setRg(rg);
        pessoa.setDataNascimento(request.dataNascimento());
        pessoa.setSenha(passwordEncoder.encode(request.senha()));
        pessoa.setTipoPerfil("PROFESSOR");
        pessoa.setCep(digitos(request.cep()));
        pessoa.setCidade(request.cidade().trim());
        pessoa.setUf(request.uf().trim().toUpperCase());
        pessoa.setEndereco(request.endereco().trim());
        pessoa.setTelefone(digitos(request.telefone()));
        pessoa = pessoaRepository.save(pessoa);

        ProfessorEntity professor = new ProfessorEntity();
        professor.setPessoa(pessoa);
        professor.setDataContratacao(request.dataContratacao());
        professor.setFormacao(request.formacao().trim());
        professor.setStatus(1);
        professor = professorRepository.save(professor);

        return paraResponse(professor);
    }

    private ProfessorAdminResponse paraResponse(ProfessorEntity professor) {
        PessoaEntity pessoa = professor.getPessoa();
        return new ProfessorAdminResponse(
                pessoa.getCodigo(),
                pessoa.getNome(),
                pessoa.getEmail(),
                professor.getFormacao(),
                professor.getDataContratacao(),
                professor.getStatus()
        );
    }

    private void validar(NovoProfessorRequest request) {
        if (request == null || vazio(request.nome()) || vazio(request.email()) || vazio(request.cpf())
                || request.dataNascimento() == null || vazio(request.senha()) || vazio(request.cep())
                || vazio(request.cidade()) || vazio(request.uf()) || vazio(request.endereco())
                || vazio(request.telefone()) || vazio(request.formacao()) || request.dataContratacao() == null) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios do professor.");
        }

        String cpf = digitos(request.cpf());
        String cep = digitos(request.cep());
        String telefone = digitos(request.telefone());
        String uf = request.uf().trim();
        String email = request.email().trim();
        String senha = request.senha();

        if (cpf.length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter 11 números.");
        }
        if (email.length() > 100 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (request.nome().trim().length() > 100 || request.formacao().trim().length() > 100) {
            throw new IllegalArgumentException("Nome e formação devem ter no máximo 100 caracteres.");
        }
        if (request.cidade().trim().length() > 50 || request.endereco().trim().length() > 100) {
            throw new IllegalArgumentException("Cidade ou endereço excede o limite de caracteres do cadastro.");
        }
        if (cep.length() != 8) {
            throw new IllegalArgumentException("O CEP deve conter 8 números.");
        }
        if (!UnidadeFederativa.contemSigla(uf)) {
            throw new IllegalArgumentException("UF inválida.");
        }
        if (telefone.isBlank() || telefone.length() > 15) {
            throw new IllegalArgumentException("O telefone deve conter no máximo 15 números.");
        }
        if (request.rg() != null && digitos(request.rg()).length() > 20) {
            throw new IllegalArgumentException("O RG deve ter no máximo 20 números.");
        }
        if (senha.length() < 8 || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha deve ter entre 8 e 72 bytes.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
