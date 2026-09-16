package kr.yuns.dropthepitchserver.credit.data.repository;

import kr.yuns.dropthepitchserver.credit.data.entity.FileAnalysisCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileAnalysisCreditRepository extends JpaRepository<FileAnalysisCredit, Long> {
    Optional<FileAnalysisCredit> findByProjectId(Long projectId);

    //내역에 프로젝트 제목이 들어가 함께 조회
    @Query("select f from FileAnalysisCredit f join fetch f.project where f.credit.id = :creditId")
    List<FileAnalysisCredit> findAllByCreditIdWithProject(@Param("creditId") Long creditId);
}
