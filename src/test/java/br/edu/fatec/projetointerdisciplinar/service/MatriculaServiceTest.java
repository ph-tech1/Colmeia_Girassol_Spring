package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private MatriculaService matriculaService;

    @Test
    void iniciaAnaliseDeMatriculaAguardando() {
        MatriculaEntity matricula = matriculaComStatus(0);
        when(matriculaRepository.findById(12)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaEntity atualizada = matriculaService.atualizarStatus(12, 1, null);

        assertEquals(1, atualizada.getStatus());
        verify(matriculaRepository).save(matricula);
    }

    @Test
    void rejeitaMatriculaComMotivoInformado() {
        MatriculaEntity matricula = matriculaComStatus(1);
        when(matriculaRepository.findById(12)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaEntity atualizada = matriculaService.atualizarStatus(12, 3, "  Documentação incompleta  ");

        assertEquals(3, atualizada.getStatus());
        assertEquals("Documentação incompleta", atualizada.getMotivoRejeicao());
    }

    @Test
    void exigeMotivoParaRejeitarMatricula() {
        assertThrows(IllegalArgumentException.class,
                () -> matriculaService.atualizarStatus(12, 3, "  "));
    }

    @Test
    void naoPermiteAprovarAntesDeIniciarAnalise() {
        MatriculaEntity matricula = matriculaComStatus(0);
        when(matriculaRepository.findById(12)).thenReturn(Optional.of(matricula));

        assertThrows(IllegalArgumentException.class,
                () -> matriculaService.atualizarStatus(12, 2, null));
    }

    @Test
    void atribuiTurmaApenasParaMatriculaAprovada() {
        MatriculaEntity matricula = matriculaComStatus(2);
        TurmaEntity turma = new TurmaEntity();
        turma.setCodigo(5);
        when(matriculaRepository.findById(12)).thenReturn(Optional.of(matricula));
        when(turmaRepository.findById(5)).thenReturn(Optional.of(turma));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaEntity atualizada = matriculaService.atribuirTurma(12, 5);

        assertEquals(turma, atualizada.getTurma());
        verify(matriculaRepository).save(matricula);
    }

    @Test
    void naoAtribuiTurmaAntesDaAprovacao() {
        MatriculaEntity matricula = matriculaComStatus(1);
        when(matriculaRepository.findById(12)).thenReturn(Optional.of(matricula));

        assertThrows(IllegalArgumentException.class,
                () -> matriculaService.atribuirTurma(12, 5));
    }

    private MatriculaEntity matriculaComStatus(Integer status) {
        MatriculaEntity matricula = new MatriculaEntity();
        matricula.setNr(12);
        matricula.setStatus(status);
        return matricula;
    }
}
