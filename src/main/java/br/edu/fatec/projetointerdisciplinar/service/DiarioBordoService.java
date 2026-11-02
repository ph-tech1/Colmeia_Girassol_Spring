package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.dto.RegistrarDiarioBordoRequest;
import br.edu.fatec.projetointerdisciplinar.model.*;
import br.edu.fatec.projetointerdisciplinar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiarioBordoService {

    private final DiarioBordoRepository diarioBordoRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final PessoaRepository pessoaRepository;
    private final ProfessorRepository professorRepository;

    @Transactional(readOnly = true)
    public List<DiarioBordoEntity> listarTodos() {
        return diarioBordoRepository.findAll();
    }

    @Transactional
    public DiarioBordoEntity salvar(RegistrarDiarioBordoRequest request, String cpfAutenticado) {
        if (request == null || request.aulaPlanejamentosCodigo() == null || request.matriculaCodigo() == null) {
            throw new IllegalArgumentException("Informe a aula e a matrícula para registrar o diário.");
        }
        validarTamanho(request.alimentacao(), 50, "Alimentação");
        validarTamanho(request.sono(), 50, "Sono");
        validarTamanho(request.humor(), 50, "Humor");
        validarTamanho(request.banheiroFralda(), 50, "Banheiro / fralda");
        validarTamanho(request.observacoes(), 500, "Observações");

        FrequenciaId id = new FrequenciaId(request.aulaPlanejamentosCodigo(), request.matriculaCodigo());
        FrequenciaEntity frequencia = frequenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registre a frequência antes de preencher o diário."));
        if (!"P".equals(frequencia.getStatusPresenca())) {
            throw new IllegalArgumentException("O diário de bordo só pode ser registrado para aluno presente.");
        }

        PessoaEntity pessoa = pessoaRepository.findByCpf(cpfAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada não encontrada."));
        ProfessorEntity professor = professorRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("O usuário autenticado não é professor."));
        if (!Integer.valueOf(1).equals(professor.getStatus())) {
            throw new IllegalArgumentException("O cadastro de professor está inativo.");
        }
        if (!professor.getPessoaCodigo().equals(frequencia.getAulaPlanejamento().getProfessor().getPessoaCodigo())) {
            throw new IllegalArgumentException("Somente o professor responsável pela aula pode registrar o diário.");
        }

        DiarioBordoEntity diario = diarioBordoRepository.findById(id).orElseGet(DiarioBordoEntity::new);
        diario.setId(id);
        diario.setFrequencia(frequencia);
        diario.setStatusPresenca("P");
        diario.setRegistradoPor(professor);
        diario.setAlimentacao(valorOpcional(request.alimentacao()));
        diario.setSono(valorOpcional(request.sono()));
        diario.setHumor(valorOpcional(request.humor()));
        diario.setBanheiroFralda(valorOpcional(request.banheiroFralda()));
        diario.setObservacoes(valorOpcional(request.observacoes()));
        return diarioBordoRepository.save(diario);
    }

    @Transactional
    public void deletar(Integer aulaCodigo, Integer matriculaCodigo, String cpfAutenticado) {
        FrequenciaId id = new FrequenciaId(aulaCodigo, matriculaCodigo);
        DiarioBordoEntity diario = diarioBordoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diário de bordo não encontrado."));
        PessoaEntity pessoa = pessoaRepository.findByCpf(cpfAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada não encontrada."));
        ProfessorEntity professor = professorRepository.findById(pessoa.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("O usuário autenticado não é professor."));
        if (!Integer.valueOf(1).equals(professor.getStatus())) {
            throw new IllegalArgumentException("O cadastro de professor está inativo.");
        }
        if (!professor.getPessoaCodigo().equals(
                diario.getFrequencia().getAulaPlanejamento().getProfessor().getPessoaCodigo())) {
            throw new IllegalArgumentException("Somente o professor responsável pela aula pode apagar o diário.");
        }
        diarioBordoRepository.delete(diario);
    }

    private void validarTamanho(String valor, int maximo, String campo) {
        if (valor != null && valor.trim().length() > maximo) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + maximo + " caracteres.");
        }
    }

    private String valorOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
