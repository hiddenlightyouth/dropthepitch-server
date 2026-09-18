package kr.yuns.dropthepitchserver.credit.data.repository;

import kr.yuns.dropthepitchserver.credit.data.dto.projection.CreditUseView;
import kr.yuns.dropthepitchserver.credit.data.entity.OpinionRequestCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpinionRequestCreditRepository extends JpaRepository<OpinionRequestCredit, Long> {
    //JPQL로 project를 조인하면 Project의 @SQLRestriction(deleted_at is null)이 붙어 내역까지 사라져서 네이티브로 처리.
    @Query(value = """
            select c.project_id as projectId,
                   p.title as projectTitle,
                   c.use_credit as useCredit,
                   p.deleted_at as projectDeletedAt,
                   c.created_at as occurredAt
            from opinion_request_credit c
            join project p on p.id = c.project_id
            where c.credit_id = :creditId
            """, nativeQuery = true)
    List<CreditUseView> findAllUsedByCreditId(@Param("creditId") Long creditId);
}
