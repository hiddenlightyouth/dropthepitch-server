package kr.yuns.dropthepitchserver.project.service;

import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.project.data.dto.response.ProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import kr.yuns.dropthepitchserver.report.data.entity.Report;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;
    private final ReportRepository reportRepository;

    private Project getProjectEntity(String email, Long projectId) {
        return projectRepository.findByIdAndUserEmail(projectId, email)
                .orElseThrow(() -> {
                    log.warn("[getProjectEntity] 프로젝트 조회 실패: projectId={}, email={}", projectId, email);
                    return new ProjectNotFoundException();
                });
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(String email, Long projectId) {
        Project project = getProjectEntity(email, projectId);

        ProjectResponseDto.FileDto file = fileRepository.findByProjectId(projectId)
                .map(f -> new ProjectResponseDto.FileDto(
                        f.getName(),
                        f.getSize(),
                        f.getType(),
                        f.getUrl(),
                        f.getThumbnailUrl()))
                .orElse(null);

        String reportId = reportRepository.findByProjectId(projectId)
                .map(Report::getUuid)
                .orElse(null);

        return new ProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getStatus(),
                project.getCreatedAt(),
                file,
                reportId);
    }
}
