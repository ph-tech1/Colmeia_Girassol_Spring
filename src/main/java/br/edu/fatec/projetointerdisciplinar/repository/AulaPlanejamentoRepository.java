package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.AulaPlanejamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AulaPlanejamentoRepository extends JpaRepository<AulaPlanejamentoEntity, Integer> {
    List<AulaPlanejamentoEntity> findByTurmaCodigo(Integer turmaCodigo);
    List<AulaPlanejamentoEntity> findByDataAula(LocalDate dataAula);
}