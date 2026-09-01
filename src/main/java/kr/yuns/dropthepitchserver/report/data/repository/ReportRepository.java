package kr.yuns.dropthepitchserver.report.data.repository;

import kr.yuns.dropthepitchserver.report.data.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
