package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.AdicionarAutorizadoBuscaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AdicionarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarAutorizadoResponsavelRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarCriancaRequest;
import br.edu.fatec.projetointerdisciplinar.dto.AtualizarPerfilResponsavelRequest;
import br.edu.fatec.projetointerdisciplinar.dto.ConsultarAutorizadoRequest;
import br.edu.fatec.projetointerdisciplinar.dto.ConsultarAutorizadoResponse;
import br.edu.fatec.projetointerdisciplinar.enums.UnidadeFederativa;
import br.edu.fatec.projetointerdisciplinar.enums.EstadoCivil;
import br.edu.fatec.projetointerdisciplinar.model.AlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoEntity;
import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.PessoaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ParentescoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import br.edu.fatec.projetointerdisciplinar.repository.AlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.AutorizadoBuscaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.FrequenciaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.PessoaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ParentescoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelAlunoRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PortalResponsavelService {

    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelAlunoRepository responsavelAlunoRepository;
    private final AutorizadoBuscaRepository autorizadoBuscaRepository;
    private final AutorizadoRepository autorizadoRepository;
    private final ParentescoRepository parentescoRepository;
    private final MatriculaRepository matriculaRepository;
    private final FrequenciaRepository frequenciaRepository;

    @Transactional
    public PessoaEntity atualizarPerfil(String cpfAutenticado, AtualizarPerfilResponsavelRequest request) {
        validarPerfil(request);
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));

        pessoa.setNome(request.nome().trim());
        pessoa.setTelefone(apenasDigitos(request.telefone()));
        pessoa.setSexo(request.sexo().trim().toUpperCase());
        pessoa.setCep(apenasDigitos(request.cep()));
        pessoa.setCidade(request.cidade().trim());
        pessoa.setUf(request.uf().trim().toUpperCase());
        pessoa.setBairro(request.bairro().trim());
        pessoa.setEndereco(request.endereco().trim());
        pessoa.setNumero(request.numero().trim());
        pessoa.setComplemento(valorOpcional(request.complemento()));
        responsavel.setLocalTrabalho(valorOpcional(request.localTrabalho()));
        responsavel.setTelefoneTrabalho(valorOpcional(apenasDigitos(request.telefoneTrabalho())));
        responsavel.setEstadoCivil(estadoCivilOuNull(request.estadoCivil()));

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
        aluno.setSexo(request.sexo().trim().toUpperCase());
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
        aluno.setSexo(request.sexo().trim().toUpperCase());
        aluno.setAlergias(valorOpcional(request.alergias()));
        aluno.setRestricoesAlimentares(valorOpcional(request.restricoesAlimentares()));
        aluno.setNecessidadesEspeciais(valorOpcional(request.necessidadesEspeciais()));
        return alunoRepository.save(aluno);
    }

    @Transactional
    public void solicitarMatricula(String cpfAutenticado, Integer alunoCodigo) {
        ResponsavelAlunoEntity vinculo = buscarVinculoResponsavel(cpfAutenticado, alunoCodigo);
        MatriculaEntity matricula = matriculaRepository.findByAlunoCodigoOrderByNrDesc(alunoCodigo)
                .stream()
                .findFirst()
                .orElse(null);
        if (matricula != null) {
            throw new IllegalArgumentException("Já existe uma matrícula para esta criança.");
        }

        matricula = new MatriculaEntity();
        matricula.setAluno(vinculo.getAluno());
        matricula.setStatus(0);
        matricula.setDataEnvio(LocalDateTime.now());
        matricula.setDataDecisao(null);
        matricula.setMotivoRejeicao(null);
        matriculaRepository.save(matricula);
    }

    @Transactional
    public void cancelarMatricula(String cpfAutenticado, Integer alunoCodigo) {
        buscarVinculoResponsavel(cpfAutenticado, alunoCodigo);
        MatriculaEntity matricula = matriculaRepository.findByAlunoCodigoOrderByNrDesc(alunoCodigo)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrada uma matrícula para cancelar."));
        if (matricula.getStatus() == null || matricula.getStatus() == 4) {
            throw new IllegalArgumentException("Esta matrícula já está cancelada ou possui status inválido.");
        }

        matricula.setStatus(4);
        matricula.setDataDecisao(LocalDateTime.now());
        matriculaRepository.save(matricula);
    }

    @Transactional
    public void retomarMatricula(String cpfAutenticado, Integer alunoCodigo) {
        buscarVinculoResponsavel(cpfAutenticado, alunoCodigo);
        MatriculaEntity matricula = matriculaRepository.findByAlunoCodigoOrderByNrDesc(alunoCodigo)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrada uma matrícula para retomar."));
        if (matricula.getStatus() == null || matricula.getStatus() != 4) {
            throw new IllegalArgumentException("Só é possível retomar uma matrícula cancelada.");
        }

        matricula.setStatus(0);
        matricula.setDataEnvio(LocalDateTime.now());
        matricula.setDataDecisao(null);
        matricula.setMotivoRejeicao(null);

        boolean possuiHistoricoDeFrequencia = !frequenciaRepository.findByMatricula_Nr(matricula.getNr()).isEmpty();
        if (!possuiHistoricoDeFrequencia) {
            matricula.setTurma(null);
        }

        matriculaRepository.save(matricula);
    }

    private ResponsavelAlunoEntity buscarVinculoResponsavel(String cpfAutenticado, Integer alunoCodigo) {
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        ResponsavelEntity responsavel = responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));
        return responsavelAlunoRepository.findById(
                        new ResponsavelAlunoId(responsavel.getPessoaCodigo(), alunoCodigo))
                .orElseThrow(() -> new IllegalArgumentException("Esta criança não está vinculada à sua conta."));
    }

    @Transactional(readOnly = true)
    public ConsultarAutorizadoResponse consultarAutorizado(
            String cpfAutenticado,
            ConsultarAutorizadoRequest request
    ) {
        validarConsultaAutorizado(request);
        String cpf = apenasDigitos(request.cpf());
        validarVinculoResponsavel(cpfAutenticado, request.alunoCodigo());
        validarNaoResponsavelDoAluno(cpf, request.alunoCodigo());

        return autorizadoRepository.findByCpf(cpf)
                .map(autorizado -> new ConsultarAutorizadoResponse(true, mascararNome(autorizado.getNome())))
                .or(() -> pessoaRepository.findByCpf(cpf)
                        .map(pessoa -> new ConsultarAutorizadoResponse(true, mascararNome(pessoa.getNome()))))
                .orElseGet(() -> new ConsultarAutorizadoResponse(false, null));
    }

    @Transactional
    public void adicionarAutorizado(String cpfAutenticado, AdicionarAutorizadoBuscaRequest request) {
        if (request == null || request.alunoCodigo() == null || vazio(request.cpf())
                || request.parentescoCodigo() == null) {
            throw new IllegalArgumentException("Informe a criança, o CPF e o parentesco da pessoa autorizada.");
        }

        String cpf = apenasDigitos(request.cpf());
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter 11 números.");
        }

        ResponsavelAlunoEntity vinculo = validarVinculoResponsavel(cpfAutenticado, request.alunoCodigo());
        validarNaoResponsavelDoAluno(cpf, request.alunoCodigo());
        ParentescoEntity parentesco = parentescoRepository.findById(request.parentescoCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Selecione um parentesco válido."));

        AutorizadoEntity autorizado = autorizadoRepository.findByCpf(cpf).orElse(null);
        if (autorizado == null) {
            PessoaEntity pessoaExistente = pessoaRepository.findByCpf(cpf).orElse(null);
            if (pessoaExistente != null) {
                if (!request.confirmarPessoaExistente()) {
                    throw new IllegalArgumentException("Confirme que a pessoa identificada pelo nome mascarado é a correta.");
                }
                autorizado = new AutorizadoEntity();
                autorizado.setCpf(cpf);
                autorizado.setPessoa(pessoaExistente);
                autorizado = autorizadoRepository.save(autorizado);
            }
        }
        if (autorizado == null) {
            String nome = request.nome() == null ? "" : request.nome().trim();
            String rg = vazio(request.rg()) ? null : apenasDigitos(request.rg());
            String telefone = apenasDigitos(request.telefone());
            if (nome.length() < 3 || nome.length() > 100
                    || (rg != null && rg.length() > 20)
                    || telefone.length() < 10 || telefone.length() > 15) {
                throw new IllegalArgumentException("Informe nome e telefone válidos para a nova pessoa autorizada.");
            }

            autorizado = new AutorizadoEntity();
            autorizado.setCpf(cpf);
            autorizado.setNome(nome);
            autorizado.setRg(rg);
            autorizado.setTelefone(telefone);
            autorizado = autorizadoRepository.save(autorizado);
        } else if (!request.confirmarPessoaExistente()) {
            throw new IllegalArgumentException("Confirme que a pessoa identificada pelo nome mascarado é a correta.");
        }

        if (autorizado.getPessoa() == null) {
            PessoaEntity pessoaExistente = pessoaRepository.findByCpf(cpf).orElse(null);
            if (pessoaExistente != null) {
                autorizado.setPessoa(pessoaExistente);
                autorizado.setNome(null);
                autorizado.setRg(null);
                autorizado.setTelefone(null);
                autorizadoRepository.save(autorizado);
            }
        }

        AutorizadoBuscaEntity autorizacao = autorizadoBuscaRepository
                .findByAutorizado_CodigoAndVinculo_Aluno_Codigo(autorizado.getCodigo(), request.alunoCodigo())
                .orElseGet(AutorizadoBuscaEntity::new);
        if (autorizacao.getCodigo() != null && Boolean.TRUE.equals(autorizacao.getAtivo())) {
            throw new IllegalArgumentException("Esta pessoa já está autorizada a buscar este aluno.");
        }

        autorizacao.setAutorizado(autorizado);
        if (autorizacao.getCodigo() == null) {
            autorizacao.setVinculo(vinculo);
        }
        autorizacao.setParentesco(parentesco);
        autorizacao.setAtivo(true);
        autorizadoBuscaRepository.save(autorizacao);
    }

    @Transactional
    public void desativarAutorizado(String cpfAutenticado, Integer autorizadoBuscaCodigo) {
        AutorizadoBuscaEntity autorizacao = buscarAutorizacaoDoResponsavel(cpfAutenticado, autorizadoBuscaCodigo);
        autorizacao.setAtivo(false);
        autorizadoBuscaRepository.save(autorizacao);
    }

    @Transactional(readOnly = true)
    public AutorizadoEdicao buscarAutorizadoParaEdicao(String cpfAutenticado, Integer autorizadoBuscaCodigo) {
        AutorizadoBuscaEntity autorizacao = buscarAutorizacaoDoResponsavel(cpfAutenticado, autorizadoBuscaCodigo);
        AutorizadoEntity autorizado = autorizacao.getAutorizado();
        boolean dadosEditaveis = autorizado.getPessoa() == null;
        return new AutorizadoEdicao(
                autorizacao.getCodigo(),
                autorizacao.getVinculo().getAluno().getNome(),
                dadosEditaveis ? autorizado.getNome() : null,
                dadosEditaveis ? autorizado.getRg() : null,
                dadosEditaveis ? autorizado.getTelefone() : null,
                autorizacao.getParentesco().getCodigo(),
                dadosEditaveis);
    }

    @Transactional
    public void atualizarAutorizado(
            String cpfAutenticado,
            Integer autorizadoBuscaCodigo,
            AtualizarAutorizadoResponsavelRequest request
    ) {
        if (request == null || request.parentescoCodigo() == null) {
            throw new IllegalArgumentException("Selecione o parentesco da pessoa autorizada.");
        }
        AutorizadoBuscaEntity autorizacao = buscarAutorizacaoDoResponsavel(cpfAutenticado, autorizadoBuscaCodigo);
        ParentescoEntity parentesco = parentescoRepository.findById(request.parentescoCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Selecione um parentesco válido."));

        AutorizadoEntity autorizado = autorizacao.getAutorizado();
        if (autorizado.getPessoa() == null) {
            String nome = request.nome() == null ? "" : request.nome().trim();
            String rg = vazio(request.rg()) ? null : apenasDigitos(request.rg());
            String telefone = apenasDigitos(request.telefone());
            if (nome.length() < 3 || nome.length() > 100
                    || (rg != null && rg.length() > 20)
                    || telefone.length() < 10 || telefone.length() > 15) {
                throw new IllegalArgumentException("Confira o nome, RG e telefone informados.");
            }
            autorizado.setNome(nome);
            autorizado.setRg(rg);
            autorizado.setTelefone(telefone);
            autorizadoRepository.save(autorizado);
        }

        autorizacao.setParentesco(parentesco);
        autorizadoBuscaRepository.save(autorizacao);
    }

    private AutorizadoBuscaEntity buscarAutorizacaoDoResponsavel(
            String cpfAutenticado,
            Integer autorizadoBuscaCodigo
    ) {
        AutorizadoBuscaEntity autorizacao = autorizadoBuscaRepository.findById(autorizadoBuscaCodigo)
                .orElseThrow(() -> new IllegalArgumentException("Autorização não encontrada."));
        ResponsavelEntity responsavel = buscarResponsavel(cpfAutenticado);
        Integer alunoCodigo = autorizacao.getVinculo().getAluno().getCodigo();
        if (!Boolean.TRUE.equals(autorizacao.getAtivo())
                || !responsavelAlunoRepository.findById(
                new ResponsavelAlunoId(responsavel.getPessoaCodigo(), alunoCodigo)).isPresent()) {
            throw new IllegalArgumentException("Autorização não encontrada.");
        }
        return autorizacao;
    }

    private ResponsavelAlunoEntity validarVinculoResponsavel(String cpfAutenticado, Integer alunoCodigo) {
        ResponsavelEntity responsavel = buscarResponsavel(cpfAutenticado);
        return responsavelAlunoRepository.findById(new ResponsavelAlunoId(responsavel.getPessoaCodigo(), alunoCodigo))
                .orElseThrow(() -> new IllegalArgumentException("Esta criança não está vinculada à sua conta."));
    }

    private ResponsavelEntity buscarResponsavel(String cpfAutenticado) {
        PessoaEntity pessoa = buscarPessoa(cpfAutenticado);
        return responsavelRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Cadastro de responsável não encontrado."));
    }

    private void validarNaoResponsavelDoAluno(String cpf, Integer alunoCodigo) {
        if (responsavelAlunoRepository.existsByResponsavel_Pessoa_CpfAndAluno_Codigo(cpf, alunoCodigo)) {
            throw new IllegalArgumentException("esta pessoa já é responsável por este aluno");
        }
    }

    public record AutorizadoEdicao(
            Integer codigo,
            String alunoNome,
            String nome,
            String rg,
            String telefone,
            Integer parentescoCodigo,
            boolean dadosEditaveis
    ) {
    }

    private void validarConsultaAutorizado(ConsultarAutorizadoRequest request) {
        if (request == null || request.alunoCodigo() == null || vazio(request.cpf())
                || apenasDigitos(request.cpf()).length() != 11) {
            throw new IllegalArgumentException("Informe uma criança e um CPF válido com 11 números.");
        }
    }

    private String mascararNome(String nome) {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) {
            return "Nome cadastrado";
        }
        StringBuilder mascarado = new StringBuilder(mascararParteNome(partes[0], true));
        for (int i = 1; i < partes.length; i++) {
            mascarado.append(' ').append(mascararParteNome(partes[i], false));
        }
        return mascarado.toString();
    }

    private String mascararParteNome(String parte, boolean primeiroNome) {
        if (parte.length() == 1) {
            return "*";
        }
        return primeiroNome
                ? parte.substring(0, Math.min(2, parte.length())) + "**"
                : parte.charAt(0) + "****";
    }

    private PessoaEntity buscarPessoa(String cpfAutenticado) {
        return pessoaRepository.findByCpf(cpfAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada não encontrada."));
    }

    private void validarPerfil(AtualizarPerfilResponsavelRequest request) {
        if (request == null || vazio(request.nome()) || vazio(request.telefone()) || vazio(request.cep())
                || vazio(request.cidade()) || vazio(request.uf()) || vazio(request.bairro())
                || vazio(request.endereco()) || vazio(request.numero()) || vazio(request.sexo())) {
            throw new IllegalArgumentException("Preencha todos os campos do perfil.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 50
                || request.cidade().trim().length() > 50
                || request.bairro().trim().length() > 60 || request.numero().trim().length() > 10
                || (request.complemento() != null && request.complemento().trim().length() > 50)
                || !UnidadeFederativa.contemSigla(request.uf()) || request.endereco().trim().length() > 100
                || (request.localTrabalho() != null && request.localTrabalho().trim().length() > 100)) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (!request.sexo().trim().matches("(?i)[FMO]")) {
            throw new IllegalArgumentException("Informe um sexo válido: F, M ou O.");
        }
        if (apenasDigitos(request.telefone()).length() < 10
                || apenasDigitos(request.telefone()).length() > 15
                || (!vazio(request.telefoneTrabalho())
                    && (apenasDigitos(request.telefoneTrabalho()).length() < 10
                    || apenasDigitos(request.telefoneTrabalho()).length() > 15))
                || apenasDigitos(request.cep()).length() != 8) {
            throw new IllegalArgumentException("Confira o telefone e o CEP informados.");
        }
        validarEstadoCivil(request.estadoCivil());
    }

    private void validarCrianca(AdicionarCriancaRequest request) {
        if (request == null || vazio(request.nome()) || request.dataNascimento() == null
                || request.parentescoCodigo() == null || vazio(request.sexo())) {
            throw new IllegalArgumentException("Informe o nome, nascimento e parentesco da criança.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 100
                || (request.alergias() != null && request.alergias().trim().length() > 100)
                || (request.restricoesAlimentares() != null && request.restricoesAlimentares().trim().length() > 100)) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (request.dataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
        }
        if (!request.sexo().trim().matches("(?i)[FMO]")) {
            throw new IllegalArgumentException("Informe um sexo válido para a criança: F, M ou O.");
        }
    }

    private void validarAtualizacaoCrianca(AtualizarCriancaRequest request) {
        if (request == null || vazio(request.nome()) || request.dataNascimento() == null || vazio(request.sexo())) {
            throw new IllegalArgumentException("Informe o nome e a data de nascimento da criança.");
        }
        if (request.nome().trim().length() < 3 || request.nome().trim().length() > 100
                || (request.alergias() != null && request.alergias().trim().length() > 100)
                || (request.restricoesAlimentares() != null && request.restricoesAlimentares().trim().length() > 100)) {
            throw new IllegalArgumentException("Um ou mais campos excedem o tamanho permitido.");
        }
        if (request.dataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
        }
        if (!request.sexo().trim().matches("(?i)[FMO]")) {
            throw new IllegalArgumentException("Informe um sexo válido para a criança: F, M ou O.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String valorOpcional(String valor) {
        return vazio(valor) ? null : valor.trim();
    }

    private EstadoCivil estadoCivilOuNull(String valor) {
        return vazio(valor) ? null : EstadoCivil.valueOf(valor.trim().toUpperCase());
    }

    private void validarEstadoCivil(String valor) {
        if (!vazio(valor)) {
            try {
                EstadoCivil.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Selecione um estado civil válido.");
            }
        }
    }
}
