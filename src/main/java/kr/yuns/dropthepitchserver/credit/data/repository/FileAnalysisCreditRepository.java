package kr.yuns.dropthepitchserver.credit.data.repository;

import kr.yuns.dropthepitchserver.credit.data.entity.FileAnalysisCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileAnalysisCreditRepository extends JpaRepository<FileAnalysisCredit, Long> {
    Optional<FileAnalysisCredit> findByProjectId(Long projectId);
}
