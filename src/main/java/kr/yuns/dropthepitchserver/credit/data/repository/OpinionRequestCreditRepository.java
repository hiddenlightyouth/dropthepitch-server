package kr.yuns.dropthepitchserver.credit.data.repository;

import kr.yuns.dropthepitchserver.credit.data.entity.OpinionRequestCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpinionRequestCreditRepository extends JpaRepository<OpinionRequestCredit, Long> {
    //내역에 프로젝트 제목이 들어가 함께 조회한다.
    @Query("select o from OpinionRequestCredit o join fetch o.project where o.credit.id = :creditId")
    List<OpinionRequestCredit> findAllByCreditIdWithProject(@Param("creditId") Long creditId);
}
