package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiarioBordoRepository extends JpaRepository<DiarioBordoEntity, Integer> {
    List<DiarioBordoEntity> findByMatriculaNr(Integer matriculaNr);
    List<DiarioBordoEntity> findByCompareceu(Boolean compareceu);
}