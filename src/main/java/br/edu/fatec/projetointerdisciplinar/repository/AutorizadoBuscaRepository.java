package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoBuscaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutorizadoBuscaRepository extends JpaRepository<AutorizadoBuscaEntity, Integer> {
    List<AutorizadoBuscaEntity> findByAluno_CodigoAndResponsavel_PessoaCodigo(
            Integer alunoCodigo,
            Integer responsavelCodigo
    );

    boolean existsByAluno_CodigoAndResponsavel_PessoaCodigoAndNomeIgnoreCaseAndTelefone(
            Integer alunoCodigo,
            Integer responsavelCodigo,
            String nome,
            String telefone
    );
}