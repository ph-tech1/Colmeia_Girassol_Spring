package br.edu.fatec.projetointerdisciplinar.repository;

import br.edu.fatec.projetointerdisciplinar.model.AutorizadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutorizadoRepository extends JpaRepository<AutorizadoEntity, Integer> {
    Optional<AutorizadoEntity> findByCpf(String cpf);
}
