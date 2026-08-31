package kr.yuns.dropthepitchserver.analyze.data.repository;

import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByProjectId(Long projectId);
}
