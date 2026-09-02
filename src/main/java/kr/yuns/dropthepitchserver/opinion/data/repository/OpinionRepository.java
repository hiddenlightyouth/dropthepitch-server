package kr.yuns.dropthepitchserver.opinion.data.repository;

import kr.yuns.dropthepitchserver.opinion.data.entity.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    Optional<Opinion> findByIdAndProject_User_Email(Long opinionId, String email);
}
