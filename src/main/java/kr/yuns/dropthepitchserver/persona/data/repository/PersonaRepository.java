package kr.yuns.dropthepitchserver.persona.data.repository;

import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    @EntityGraph(attributePaths = {
            "personaBasic",
            "personaEconomy",
            "personaPsychology",
            "personaLifestyle",
            "personaDigital",
            "personaDecision",
            "personaTags"
    })
    Optional<Persona> findDetailById(Long id);

    @EntityGraph(attributePaths = {
            "personaBasic",
            "personaEconomy",
            "personaPsychology",
            "personaLifestyle",
            "personaDigital",
            "personaDecision",
            "personaTags"
    })
    @Query("select p from Persona p")
    List<Persona> findAllDetail();
}
