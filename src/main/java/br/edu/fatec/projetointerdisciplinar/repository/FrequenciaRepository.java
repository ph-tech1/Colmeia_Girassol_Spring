package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.FrequenciaEntity;
import br.edu.fatec.projetointerdisciplinar.model.FrequenciaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequenciaRepository extends JpaRepository<FrequenciaEntity, FrequenciaId> {
    List<FrequenciaEntity> findByIdAulaPlanejamentosCodigo(Integer aulaCodigo);
    List<FrequenciaEntity> findByIdMatriculaCodigo(Integer matriculaCodigo);
    List<FrequenciaEntity> findByMatricula_NrOrderByAulaPlanejamento_DataAulaDesc(Integer matriculaCodigo);
    List<FrequenciaEntity> findByMatricula_Nr(Integer matriculaCodigo);
}