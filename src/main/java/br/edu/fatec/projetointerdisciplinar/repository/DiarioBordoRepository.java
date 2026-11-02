package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.DiarioBordoEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiarioBordoRepository extends JpaRepository<DiarioBordoEntity, FrequenciaId> {
    List<DiarioBordoEntity> findByFrequencia_Matricula_Nr(Integer matriculaNr);
}