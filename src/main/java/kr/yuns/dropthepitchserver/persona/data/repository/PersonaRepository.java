package kr.yuns.dropthepitchserver.persona.data.repository;

import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
}
