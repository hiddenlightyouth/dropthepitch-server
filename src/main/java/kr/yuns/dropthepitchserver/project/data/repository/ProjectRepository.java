package kr.yuns.dropthepitchserver.project.data.repository;

import kr.yuns.dropthepitchserver.project.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
