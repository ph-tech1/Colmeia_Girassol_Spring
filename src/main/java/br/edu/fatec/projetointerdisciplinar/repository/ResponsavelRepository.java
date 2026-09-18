package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponsavelRepository extends JpaRepository<ResponsavelEntity, Integer> {
    List<ResponsavelEntity> findByStatus(Integer status);
}