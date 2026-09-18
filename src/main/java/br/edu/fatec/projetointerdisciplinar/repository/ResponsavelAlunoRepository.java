package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoEntity;
import br.edu.fatec.projetointerdisciplinar.model.ResponsavelAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponsavelAlunoRepository extends JpaRepository<ResponsavelAlunoEntity, ResponsavelAlunoId> {
    List<ResponsavelAlunoEntity> findByIdAlunoCodigo(Integer alunoCodigo);
    List<ResponsavelAlunoEntity> findByIdResponsavelCodigo(Integer responsavelCodigo);
}