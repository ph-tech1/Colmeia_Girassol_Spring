package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.TurmaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<TurmaEntity, Integer> {
    List<TurmaEntity> findByProfessorPessoaCodigo(Integer professorCodigo);
    List<TurmaEntity> findByAno(String ano);
}