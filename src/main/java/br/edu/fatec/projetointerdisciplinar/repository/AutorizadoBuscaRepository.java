package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorizadoBuscaRepository extends JpaRepository<AutorizadoBuscaEntity, Integer> {
    List<AutorizadoBuscaEntity> findByAlunoCodigo(Integer alunoCodigo);
}