package kr.yuns.dropthepitchserver.opinion.data.repository;

import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    Optional<Opinion> findByIdAndProject_User_Email(Long opinionId, String email);

    //연령대별 점수 계산에 페르소나 나이가 필요해 함께 조회.
    @Query("select o from Opinion o join fetch o.persona where o.project.id = :projectId")
    List<Opinion> findAllByProjectIdWithPersona(@Param("projectId") Long projectId);
}
