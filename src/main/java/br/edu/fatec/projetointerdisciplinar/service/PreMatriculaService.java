package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.*;
import br.edu.fatec.projetointerdisciplinar.model.*;
import br.edu.fatec.projetointerdisciplinar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreMatriculaService {

    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;

    @Transactional
    public PreMatriculaResponse cadastrar(PreMatriculaRequest request) {
        validar(request);

        pessoaRepository.findByCpf(apenasDigitos(request.cpfResponsavel())).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este CPF.");
        });
        pessoaRepository.findByRg(apenasDigitos(request.rgResponsavel())).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este RG.");
        });

        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setNome(request.nomeResponsavel().trim());
        pessoa.setCpf(apenasDigitos(request.cpfResponsavel()));
        pessoa.setRg(apenasDigitos(request.rgResponsavel()));
        pessoa.setDataNascimento(request.dataNascimentoResponsavel());
        pessoa.setSenha(request.senha());
        pessoa.setCep(apenasDigitos(request.cep()));
        pessoa.setCidade(request.cidade().trim());
        pessoa.setUf(request.uf().trim().toUpperCase());
        pessoa.setEndereco(request.endereco().trim());
        pessoa.setTelefone(apenasDigitos(request.telefone()));
        pessoa = pessoaRepository.save(pessoa);

        ResponsavelEntity responsavel = new ResponsavelEntity();
        responsavel.setPessoaCodigo(pessoa.getCodigo());
        responsavel.setPessoa(pessoa);
        responsavel.setLocalTrabalho(request.localTrabalho().trim());
        responsavel.setTelefoneTrabalho(apenasDigitos(request.telefoneTrabalho()));
        responsavel.setEstadoCivil(request.estadoCivil().trim());
        responsavel.setStatus(0);
        responsavel = responsavelRepository.save(responsavel);

        AlunoEntity aluno = new AlunoEntity();
        aluno.setNome(request.nomeAluno().trim());
        aluno.setDataNascimento(request.dataNascimentoAluno());
        aluno.setAlergias(valorOuNenhum(request.alergias()));
        aluno.setRestricoesAlimentar(valorOuNenhum(request.restricoesAlimentares()));
        aluno.setNecessidadesEspeciais(valorOuNenhum(request.necessidadesEspeciais()));
        aluno = alunoRepository.save(aluno);

        ResponsavelAlunoEntity vinculo = new ResponsavelAlunoEntity();
        vinculo.setId(new ResponsavelAlunoId(responsavel.getPessoaCodigo(), aluno.getCodigo()));
        vinculo.setResponsavel(responsavel);
        vinculo.setAluno(aluno);
        vinculo.setGrauParentesco(request.grauParentesco().trim());
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
                || vazio(request.rgResponsavel()) || request.dataNascimentoResponsavel() == null
                || vazio(request.senha()) || vazio(request.cep()) || vazio(request.cidade())
                || vazio(request.uf()) || vazio(request.endereco()) || vazio(request.telefone())
                || vazio(request.localTrabalho()) || vazio(request.telefoneTrabalho())
                || vazio(request.estadoCivil()) || vazio(request.nomeAluno())
                || request.dataNascimentoAluno() == null || vazio(request.grauParentesco())) {
            throw new IllegalArgumentException("Preencha todos os campos obrigatórios da pré-matrícula.");
        }
        if (apenasDigitos(request.cpfResponsavel()).length() != 11) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        if (apenasDigitos(request.rgResponsavel()).length() > 8) {
            throw new IllegalArgumentException("RG inválido.");
        }
        if (apenasDigitos(request.cep()).length() != 8) {
            throw new IllegalArgumentException("CEP inválido.");
        }
        if (request.uf().trim().length() != 2) {
            throw new IllegalArgumentException("UF inválida.");
        }
    }

    private boolean vazio(String value) {
        return value == null || value.isBlank();
    }

    private String apenasDigitos(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String valorOuNenhum(String value) {
        return vazio(value) ? "Nenhuma" : value.trim();
    }
}
