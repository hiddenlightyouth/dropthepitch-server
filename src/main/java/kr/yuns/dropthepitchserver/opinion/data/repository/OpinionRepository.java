package kr.yuns.dropthepitchserver.opinion.data.repository;

import kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget;
import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    Optional<Opinion> findByIdAndProject_User_Email(Long opinionId, String email);

    //페르소나가 중복 선별되는 것을 막기 위해 확인.
    boolean existsByProject_Id(Long projectId);

    //교체할 대상
    Optional<Opinion> findByProject_IdAndPersona_IdAndProject_User_Email(Long projectId, Long personaId, String email);


    //이미 포함 되있는 페르소나 Id 수집
    @Query("select o.persona.id from Opinion o where o.project.id = :projectId")
    List<Long> findPersonaIdsByProjectId(@Param("projectId") Long projectId);

    //연령대별 점수 계산에 페르소나 나이가 필요해 함께 조회.
    @Query("select o from Opinion o join fetch o.persona where o.project.id = :projectId")
    List<Opinion> findAllByProjectIdWithPersona(@Param("projectId") Long projectId);

    @Query("""
            select new kr.yuns.dropthepitchserver.opinion.data.dto.projection.OpinionCollectionTarget(o.id, o.persona.id)
              from Opinion o
             where o.project.id = :projectId
               and o.sentiment is null
            """)
    List<OpinionCollectionTarget> findCollectionTargets(@Param("projectId") Long projectId);
    //Persona의 1:1 연관관계가 즉시 로딩이라 함께 조회하지 않으면 페르소나마다 추가 조회가 발생.
    //컬렉션 두 개를 동시에 fetch join 할 수 없어 상세 의견은 아래에서 따로 조회.
    @Query("select distinct o from Opinion o " +
            "join fetch o.persona p " +
            "left join fetch p.personaBasic " +
            "left join fetch p.personaEconomy " +
            "left join fetch p.personaPsychology " +
            "left join fetch p.personaLifestyle " +
            "left join fetch p.personaDigital " +
            "left join fetch p.personaDecision " +
            "left join fetch p.personaTags " +
            "where o.project.id = :projectId and o.project.user.email = :email")
    List<Opinion> findAllByProjectIdWithPersonaAndTags(@Param("projectId") Long projectId,
                                                       @Param("email") String email);

    //같은 영속성 컨텍스트에서 위 조회 결과의 상세 의견을 채우기 위한 조회.
    @Query("select distinct o from Opinion o " +
            "left join fetch o.opinionDetails " +
            "where o.project.id = :projectId")
    List<Opinion> findAllByProjectIdWithDetails(@Param("projectId") Long projectId);
}
