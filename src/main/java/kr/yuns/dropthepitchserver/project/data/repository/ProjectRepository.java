package kr.yuns.dropthepitchserver.project.data.repository;

import jakarta.persistence.LockModeType;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndUserEmail(Long id, String email);

    //동시에 두 번 삭제되지 않도록 행을 잠근다. 늦게 온 요청은 앞 요청이 끝난 뒤 삭제된 행을 보고 404가 된다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findWithLockByIdAndUserEmail(Long id, String email);

    List<Project> findAllByUserOrderByUpdatedAtDesc(User user);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update Project p
               set p.opinionCollectionStatus = :next
             where p.id = :projectId
               and p.opinionCollectionStatus in :expected
            """)
    int updateOpinionCollectionStatus(@Param("projectId") Long projectId,
                                      @Param("expected") Collection<OpinionCollectionStatus> expected,
                                      @Param("next") OpinionCollectionStatus next);

    default boolean startOpinionCollection(Long projectId) {
        return updateOpinionCollectionStatus(projectId,
                List.of(OpinionCollectionStatus.NOT_STARTED, OpinionCollectionStatus.FAILED),
                OpinionCollectionStatus.IN_PROGRESS) == 1;
    }

    default void finishOpinionCollection(Long projectId, OpinionCollectionStatus next) {
        updateOpinionCollectionStatus(projectId,
                List.of(OpinionCollectionStatus.IN_PROGRESS), next);
    }
}
