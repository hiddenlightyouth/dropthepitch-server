package kr.yuns.dropthepitchserver.analyze.data.repository;

import kr.yuns.dropthepitchserver.analyze.data.entity.AnalysisTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisTimelineRepository extends JpaRepository<AnalysisTimeline, Long> {
}
