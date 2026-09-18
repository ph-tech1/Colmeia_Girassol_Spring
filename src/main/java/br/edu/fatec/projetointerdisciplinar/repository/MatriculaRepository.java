package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.MatriculaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<MatriculaEntity, Integer> {
    List<MatriculaEntity> findByAlunoCodigo(Integer alunoCodigo);
    List<MatriculaEntity> findByTurmaCodigo(Integer turmaCodigo);
    List<MatriculaEntity> findByStatus(Integer status);
}