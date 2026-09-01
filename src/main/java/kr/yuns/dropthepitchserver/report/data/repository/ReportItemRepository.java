package kr.yuns.dropthepitchserver.report.data.repository;

import kr.yuns.dropthepitchserver.report.data.entity.ReportItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportItemRepository extends JpaRepository<ReportItem, Long> {
}
