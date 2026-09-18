package kr.yuns.dropthepitchserver.analyze.data.repository;

import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//피그마 상에서 얘기 했을 때 UUID가 나와서 테이블에 추가 해둔거 같은데 api 명세서에는 projectId로 조회 하는걸로 되어있어서 일단은 두개다 추가해둠
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Optional<Analysis> findByUuid(String uuid);
    Optional<Analysis> findByProject_IdAndProject_User_Email(Long projectId, String email);
    Optional<Analysis> findByProjectId(Long projectId);
}
