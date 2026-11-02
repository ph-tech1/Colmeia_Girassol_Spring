package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorizadoBuscaRepository extends JpaRepository<AutorizadoBuscaEntity, Integer> {
    List<AutorizadoBuscaEntity> findByVinculo_Aluno_CodigoAndVinculo_Responsavel_PessoaCodigoAndAtivoTrue(
            Integer alunoCodigo,
            Integer responsavelCodigo
    );

    List<AutorizadoBuscaEntity> findByVinculo_Aluno_CodigoAndAtivoTrue(Integer alunoCodigo);

    Optional<AutorizadoBuscaEntity> findByAutorizado_CodigoAndVinculo_Aluno_Codigo(
            Integer autorizadoCodigo,
            Integer alunoCodigo
    );
}