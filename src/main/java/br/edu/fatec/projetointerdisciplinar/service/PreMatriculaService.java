package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.*;
import br.edu.fatec.projetointerdisciplinar.enums.EstadoCivil;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.*;
import br.edu.fatec.projetointerdisciplinar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreMatriculaService {

    private final PessoaRepository pessoaRepository;
    private final AutorizadoRepository autorizadoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;
    private final ParentescoRepository parentescoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PreMatriculaResponse cadastrar(PreMatriculaRequest request) {
        validar(request);
        String rgDigits = apenasDigitos(request.rgResponsavel());
        String rg = rgDigits.isBlank() ? null : rgDigits;

        pessoaRepository.findByCpf(apenasDigitos(request.cpfResponsavel())).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este CPF.");
        });
        pessoaRepository.findByEmail(request.email().trim()).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este email.");
        });
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setNome(request.nomeResponsavel().trim());
        pessoa.setEmail(request.email().trim());
        pessoa.setCpf(apenasDigitos(request.cpfResponsavel()));
        pessoa.setRg(rg);
        pessoa.setDataNascimento(request.dataNascimentoResponsavel());
        pessoa.setSexo(request.sexoResponsavel().trim().toUpperCase());
        pessoa.setSenha(passwordEncoder.encode(request.senha()));
        pessoa.setTipoPerfil("RESPONSAVEL");
        pessoa.setCep(apenasDigitos(request.cep()));
        pessoa.setCidade(request.cidade().trim());
        pessoa.setUf(request.uf().trim().toUpperCase());
        pessoa.setBairro(request.bairro().trim());
        pessoa.setEndereco(request.endereco().trim());
        pessoa.setNumero(request.numero().trim());
        pessoa.setComplemento(valorOpcional(request.complemento()));
        pessoa.setTelefone(apenasDigitos(request.telefone()));
        pessoa = pessoaRepository.save(pessoa);
        PessoaEntity pessoaSalva = pessoa;

        ResponsavelEntity responsavel = new ResponsavelEntity();
        responsavel.setPessoa(pessoa);
        responsavel.setLocalTrabalho(valorOpcional(request.localTrabalho()));
        responsavel.setTelefoneTrabalho(valorOpcional(apenasDigitos(request.telefoneTrabalho())));
        responsavel.setEstadoCivil(estadoCivilOuNull(request.estadoCivil()));
        responsavel.setStatus(1);
        responsavel = responsavelRepository.save(responsavel);

        autorizadoRepository.findByCpf(pessoaSalva.getCpf()).ifPresent(autorizado -> {
            autorizado.setPessoa(pessoaSalva);
            autorizado.setNome(null);
            autorizado.setRg(null);
            autorizado.setTelefone(null);
            autorizadoRepository.save(autorizado);
        });

        AlunoEntity aluno = new AlunoEntity();
        aluno.setNome(request.nomeAluno().trim());
        aluno.setDataNascimento(request.dataNascimentoAluno());
        aluno.setSexo(request.sexoAluno().trim().toUpperCase());
        aluno.setAlergias(valorOpcional(request.alergias()));
        aluno.setRestricoesAlimentares(valorOpcional(request.restricoesAlimentares()));
        aluno.setNecessidadesEspeciais(valorOpcional(request.necessidadesEspeciais()));
        aluno = alunoRepository.save(aluno);

        ResponsavelAlunoEntity vinculo = new ResponsavelAlunoEntity();
        vinculo.setId(new ResponsavelAlunoId(responsavel.getPessoaCodigo(), aluno.getCodigo()));
        vinculo.setResponsavel(responsavel);
        vinculo.setAluno(aluno);
        vinculo.setParentesco(parentescoRepository.findById(request.parentescoCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Selecione um parentesco válido.")));
        vinculo.setEspFinanceiro(true);
        vinculo.setOrdemContato(1);
        responsavelAlunoRepository.save(vinculo);

        return new PreMatriculaResponse(
                responsavel.getPessoaCodigo(),
                aluno.getCodigo(),
                "Pré-matrícula cadastrada com sucesso."
        );
    }

    private void validar(PreMatriculaRequest request) {
        if (request == null || vazio(request.nomeResponsavel()) || vazio(request.cpfResponsavel())
                || request.dataNascimentoResponsavel() == null
                || vazio(request.sexoResponsavel()) || vazio(request.senha()) || vazio(request.cep()) || vazio(request.cidade())
                || vazio(request.uf()) || vazio(request.bairro()) || vazio(request.endereco()) || vazio(request.numero())
                || vazio(request.telefone()) || vazio(request.email()) || vazio(request.nomeAluno())
                || request.dataNascimentoAluno() == null || vazio(request.sexoAluno())
                || request.parentescoCodigo() == null) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios da pré-matrícula.");
        }
        if (request.email().trim().length() > 100 || !request.email().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (request.nomeResponsavel().trim().length() > 100 || request.nomeAluno().trim().length() > 100
                || request.cidade().trim().length() > 50 || request.uf().trim().length() > 2
                || request.telefone().trim().length() > 15) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (!request.sexoResponsavel().trim().matches("(?i)[FMO]")) {
            throw new IllegalArgumentException("Informe um sexo válido: F, M ou O.");
        }
        if (!request.sexoAluno().trim().matches("(?i)[FMO]")) {
            throw new IllegalArgumentException("Informe um sexo válido para o aluno: F, M ou O.");
        }
        if (apenasDigitos(request.cpfResponsavel()).length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter 11 números.");
        }
        if (apenasDigitos(request.cep()).length() != 8) {
            throw new IllegalArgumentException("CEP inválido.");
        }
        if (!UnidadeFederativa.contemSigla(request.uf())) {
            throw new IllegalArgumentException("UF inválida.");
        }
        if (request.bairro().trim().length() > 60 || request.endereco().trim().length() > 100
                || request.numero().trim().length() > 10
                || (request.complemento() != null && request.complemento().trim().length() > 50)
                || (request.localTrabalho() != null && request.localTrabalho().trim().length() > 100)
                || (request.alergias() != null && request.alergias().trim().length() > 100)
                || (request.restricoesAlimentares() != null && request.restricoesAlimentares().trim().length() > 100)) {
            throw new IllegalArgumentException("Confira os limites de tamanho do endereço ou local de trabalho.");
        }
        if (!vazio(request.telefoneTrabalho())) {
            int telefoneTrabalho = apenasDigitos(request.telefoneTrabalho()).length();
            if (telefoneTrabalho < 10 || telefoneTrabalho > 15) {
                throw new IllegalArgumentException("O telefone de trabalho deve conter entre 10 e 15 números.");
            }
        }
        validarEstadoCivil(request.estadoCivil());
    }

    private boolean vazio(String value) {
        return value == null || value.isBlank();
    }

    private String apenasDigitos(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String valorOpcional(String value) {
        return vazio(value) ? null : value.trim();
    }

    private EstadoCivil estadoCivilOuNull(String value) {
        return vazio(value) ? null : EstadoCivil.valueOf(value.trim().toUpperCase());
    }

    private void validarEstadoCivil(String value) {
        if (!vazio(value)) {
            try {
                EstadoCivil.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Selecione um estado civil válido.");
            }
        }
    }
}
