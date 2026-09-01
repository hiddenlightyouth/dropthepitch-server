package kr.yuns.dropthepitchserver.opinion.data.repository;

import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
}
