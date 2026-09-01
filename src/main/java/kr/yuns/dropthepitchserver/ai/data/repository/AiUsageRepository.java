package kr.yuns.dropthepitchserver.ai.data.repository;

import kr.yuns.dropthepitchserver.ai.data.entity.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {
    List<AiUsage> findAllByProjectId(Long projectId);
}
