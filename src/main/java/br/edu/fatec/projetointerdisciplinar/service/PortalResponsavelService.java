package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.AdicionarAutorizadoBuscaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AdicionarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarPerfilResponsavelRequest;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
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
    private final AutorizadoBuscaRepository autorizadoBuscaRepository;

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

    @Transactional
    public AlunoEntity atualizarCrianca(String cpfAutenticado, Integer alunoCodigo, AtualizarCriancaRequest request) {
        validarAtualizacaoCrianca(request);
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));
        ResponsavelAlunoEntity vinculo = responsavelAlunoRepository.findById(
                        new ResponsavelAlunoId(responsavel.getPessoaCodigo(), alunoCodigo))
                .orElseThrow(() -> new IllegalArgumentException("Esta criança não está vinculada à sua conta."));

        AlunoEntity aluno = vinculo.getAluno();
        aluno.setNome(request.nome().trim());
        aluno.setDataNascimento(request.dataNascimento());
        aluno.setAlergias(valorOuNenhum(request.alergias()));
        aluno.setRestricoesAlimentar(valorOuNenhum(request.restricoesAlimentares()));
        aluno.setNecessidadesEspeciais(valorOuNenhum(request.necessidadesEspeciais()));
        return alunoRepository.save(aluno);
    }

    @Transactional
    public void adicionarAutorizado(String cpfAutenticado, AdicionarAutorizadoBuscaRequest request) {
        if (request == null || request.alunoCodigo() == null || vazio(request.nome())
                || vazio(request.telefone())
                || vazio(request.grauParentesco())) {
            throw new IllegalArgumentException("Informe a criança, o nome, o telefone e o vínculo da pessoa autorizada.");
        }

        String nome = request.nome().trim();
        String telefone = apenasDigitos(request.telefone());
        String grauParentesco = request.grauParentesco().trim();
        if (nome.length() < 3 || nome.length() > 100
                || telefone.length() < 10 || telefone.length() > 15
                || grauParentesco.length() > 50) {
            throw new IllegalArgumentException("Confira o nome, o telefone e o vínculo informados.");
        }

        PessoaEntity pessoaResponsavel = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoaResponsavel.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));
        ResponsavelAlunoEntity vinculo = responsavelAlunoRepository.findById(
                        new ResponsavelAlunoId(responsavel.getPessoaCodigo(), request.alunoCodigo()))
                .orElseThrow(() -> new IllegalArgumentException("Esta criança não está vinculada à sua conta."));
        if (autorizadoBuscaRepository
                .existsByAluno_CodigoAndResponsavel_PessoaCodigoAndNomeIgnoreCaseAndTelefone(
                        request.alunoCodigo(), responsavel.getPessoaCodigo(), nome, telefone)) {
            throw new IllegalArgumentException("Essa pessoa já está autorizada a buscar esta criança.");
        }

        AutorizadoBuscaEntity autorizado = new AutorizadoBuscaEntity();
        autorizado.setNome(nome);
        autorizado.setTelefone(telefone);
        autorizado.setAluno(vinculo.getAluno());
        autorizado.setResponsavel(responsavel);
        autorizado.setGrauParentesco(grauParentesco);
        autorizadoBuscaRepository.save(autorizado);
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

    private void validarAtualizacaoCrianca(AtualizarCriancaRequest request) {
        if (request == null || vazio(request.nome()) || request.dataNascimento() == null) {
            throw new IllegalArgumentException("Informe o nome e a data de nascimento da criança.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 100
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
