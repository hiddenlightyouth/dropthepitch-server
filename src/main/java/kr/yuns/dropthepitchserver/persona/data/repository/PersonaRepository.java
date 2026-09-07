package kr.yuns.dropthepitchserver.persona.data.repository;

import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    //선별 태그와 겹치는 개수가 많은 순으로 한 연령대에서 선정 left join으로 태그가 안 겹쳐도 0점으로 처리 동점처리시 랜덤으로 선정
    // excludedIds로 이미 뽑힌 페르소나 구분.
    @Query(value = """
            select p.id from persona p
            left join persona_tag pt on pt.persona_id = p.id and pt.name in (:tags)
            where p.age between :startAge and :endAge
              and p.id not in (:excludedIds)
            group by p.id
            order by count(pt.id) desc, rand()
            limit :limit
            """, nativeQuery = true)
    List<Long> findTopMatchedIds(@Param("tags") List<String> tags,
                                 @Param("startAge") int startAge,
                                 @Param("endAge") int endAge,
                                 @Param("excludedIds") List<Long> excludedIds, //뽑힌  페르소나 Id
                                 @Param("limit") int limit);
}
