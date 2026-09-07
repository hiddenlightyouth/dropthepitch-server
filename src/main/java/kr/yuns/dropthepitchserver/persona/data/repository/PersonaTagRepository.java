package kr.yuns.dropthepitchserver.persona.data.repository;

import kr.yuns.dropthepitchserver.persona.data.entity.PersonaTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonaTagRepository extends JpaRepository<PersonaTag, Long> {
     //페르소나가 가진 모든 태그를 조회. (중복 제거) return 태그 이름 목록
    @Query("select distinct pt.name from PersonaTag pt order by pt.name")
    List<String> findDistinctNames();
}
