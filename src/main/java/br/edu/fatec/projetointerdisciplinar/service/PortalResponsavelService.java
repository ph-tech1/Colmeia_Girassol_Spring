package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.AdicionarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarPerfilResponsavelRequest;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelAlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PortalResponsavelService {

    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;

    @Transactional
    public PessoaEntity atualizarPerfil(String cpfAutenticado, AtualizarPerfilResponsavelRequest request) {
        validarPerfil(request);
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));

        pessoa.setNome(request.nome().trim());
        pessoa.setTelefone(apenasDigitos(request.telefone()));
        pessoa.setCep(apenasDigitos(request.cep()));
        pessoa.setCidade(request.cidade().trim());
        pessoa.setUf(request.uf().trim().toUpperCase());
        pessoa.setEndereco(request.endereco().trim());
        responsavel.setLocalTrabalho(request.localTrabalho().trim());
        responsavel.setTelefoneTrabalho(apenasDigitos(request.telefoneTrabalho()));
        responsavel.setEstadoCivil(request.estadoCivil().trim());

        pessoaRepository.save(pessoa);
        responsavelRepository.save(responsavel);
        return pessoa;
    }

    @Transactional
    public AlunoEntity adicionarCrianca(String cpfAutenticado, AdicionarCriancaRequest request) {
        validarCrianca(request);
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));

        AlunoEntity aluno = new AlunoEntity();
        aluno.setNome(request.nome().trim());
        aluno.setDataNascimento(request.dataNascimento());
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
        return aluno;
    }

    private PessoaEntity buscarPessoa(String cpfAutenticado) {
        return pessoaRepository.findByCpf(cpfAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada não encontrada."));
    }

    private void validarPerfil(AtualizarPerfilResponsavelRequest request) {
        if (request == null || vazio(request.nome()) || vazio(request.telefone()) || vazio(request.cep())
                || vazio(request.cidade()) || vazio(request.uf()) || vazio(request.endereco())
                || vazio(request.localTrabalho()) || vazio(request.telefoneTrabalho()) || vazio(request.estadoCivil())) {
            throw new IllegalArgumentException("Preencha todos os campos do perfil.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 50
                || request.cidade().trim().length() > 50
                || !UnidadeFederativa.contemSigla(request.uf()) || request.endereco().trim().length() > 100
                || request.localTrabalho().trim().length() > 50 || request.estadoCivil().trim().length() > 50) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (apenasDigitos(request.telefone()).length() < 10
                || apenasDigitos(request.telefone()).length() > 15
                || apenasDigitos(request.telefoneTrabalho()).length() < 10
                || apenasDigitos(request.telefoneTrabalho()).length() > 15
                || apenasDigitos(request.cep()).length() != 8) {
            throw new IllegalArgumentException("Confira o telefone e o CEP informados.");
        }
    }

    private void validarCrianca(AdicionarCriancaRequest request) {
        if (request == null || vazio(request.nome()) || request.dataNascimento() == null
                || vazio(request.grauParentesco())) {
            throw new IllegalArgumentException("Informe o nome, nascimento e parentesco da criança.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 100
                || request.grauParentesco().trim().length() < 2 || request.grauParentesco().trim().length() > 20
                || (request.alergias() != null && request.alergias().trim().length() > 50)
                || (request.restricoesAlimentares() != null && request.restricoesAlimentares().trim().length() > 50)) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (request.dataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String valorOuNenhum(String valor) {
        return vazio(valor) ? "Nenhuma" : valor.trim();
    }
}
