package kr.yuns.dropthepitchserver.report.data.repository;

import kr.yuns.dropthepitchserver.report.data.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByProjectId(Long projectId);

    Optional<Report> findByUuidAndProject_User_Email(String uuid, String email);
}
