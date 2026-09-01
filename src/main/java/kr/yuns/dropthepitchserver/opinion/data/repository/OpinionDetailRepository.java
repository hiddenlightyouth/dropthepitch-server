package kr.yuns.dropthepitchserver.opinion.data.repository;

import kr.yuns.dropthepitchserver.opinion.data.entity.OpinionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpinionDetailRepository extends JpaRepository<OpinionDetail, Long> {
}
