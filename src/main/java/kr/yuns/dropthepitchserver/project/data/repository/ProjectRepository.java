package kr.yuns.dropthepitchserver.project.data.repository;

import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndUserEmail(Long id, String email);
    List<Project> findAllByUserOrderByUpdatedAtDesc(User user);
}
