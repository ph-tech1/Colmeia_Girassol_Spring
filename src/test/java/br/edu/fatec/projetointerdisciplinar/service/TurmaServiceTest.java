package br.edu.fatec.projetointerdisciplinar.service;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import br.edu.fatec.projetointerdisciplinar.model.ProfessorEntity;
import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import br.edu.fatec.projetointerdisciplinar.repository.MatriculaRepository;
import br.edu.fatec.projetointerdisciplinar.repository.ProfessorRepository;
import br.edu.fatec.projetointerdisciplinar.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private TurmaService turmaService;

    @Test
    void savesClassWithManagedActiveTeacher() {
        ProfessorEntity selectedTeacher = new ProfessorEntity();
        selectedTeacher.setPessoaCodigo(23);
        selectedTeacher.setStatus(1);

        ProfessorEntity requestedTeacher = new ProfessorEntity();
        requestedTeacher.setPessoaCodigo(23);

        TurmaEntity turma = new TurmaEntity();
        turma.setNomeTurma("Jardim das Abelhinhas");
        turma.setAno(2026);
        turma.setGrau("Infantil");
        turma.setProfessor(requestedTeacher);

        when(professorRepository.findById(23)).thenReturn(Optional.of(selectedTeacher));
        when(turmaRepository.save(turma)).thenReturn(turma);

        TurmaEntity saved = turmaService.salvar(turma);

        assertSame(selectedTeacher, saved.getProfessor());
        assertEquals("Jardim das Abelhinhas", saved.getNomeTurma());
        verify(turmaRepository).save(turma);
    }

    @Test
    void updatesExistingClassWithManagedActiveTeacher() {
        ProfessorEntity activeTeacher = new ProfessorEntity();
        activeTeacher.setPessoaCodigo(23);
        activeTeacher.setStatus(1);
        ProfessorEntity requestedTeacher = new ProfessorEntity();
        requestedTeacher.setPessoaCodigo(23);

        TurmaEntity existing = new TurmaEntity();
        existing.setCodigo(7);
        TurmaEntity changes = new TurmaEntity();
        changes.setNomeTurma("  Jardim renovado  ");
        changes.setAno(2027);
        changes.setGrau("Infantil");
        changes.setProfessor(requestedTeacher);

        when(turmaRepository.findById(7)).thenReturn(Optional.of(existing));
        when(professorRepository.findById(23)).thenReturn(Optional.of(activeTeacher));
        when(turmaRepository.save(existing)).thenReturn(existing);

        TurmaEntity updated = turmaService.atualizar(7, changes);

        assertSame(existing, updated);
        assertEquals("Jardim renovado", updated.getNomeTurma());
        assertEquals(2027, updated.getAno());
        assertEquals("Infantil", updated.getGrau());
        assertSame(activeTeacher, updated.getProfessor());
        verify(turmaRepository).save(existing);
    }

    @Test
    void deletesClassAndUnassignsLinkedEnrollmentsWithoutDeletingThem() {
        TurmaEntity turma = new TurmaEntity();
        turma.setCodigo(7);
        MatriculaEntity matricula = new MatriculaEntity();
        matricula.setTurma(turma);
        when(turmaRepository.findById(7)).thenReturn(Optional.of(turma));
        when(matriculaRepository.findByTurmaCodigo(7)).thenReturn(List.of(matricula));

        turmaService.deletar(7);

        assertNull(matricula.getTurma());
        verify(matriculaRepository).saveAll(List.of(matricula));
        verify(turmaRepository).delete(turma);
    }

    @Test
    void deletesClassWithoutSavingEmptyEnrollmentList() {
        TurmaEntity turma = new TurmaEntity();
        turma.setCodigo(7);
        when(turmaRepository.findById(7)).thenReturn(Optional.of(turma));
        when(matriculaRepository.findByTurmaCodigo(7)).thenReturn(List.of());

        turmaService.deletar(7);

        verify(matriculaRepository, never()).saveAll(anyList());
        verify(turmaRepository).delete(turma);
    }
}
