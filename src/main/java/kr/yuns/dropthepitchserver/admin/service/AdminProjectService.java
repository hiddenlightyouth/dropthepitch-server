package kr.yuns.dropthepitchserver.admin.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminProjectQueryRepository;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.opinion.data.repository.OpinionRepository;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import kr.yuns.dropthepitchserver.project.event.ProjectDeletedEvent;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProjectService {
    private final AdminProjectQueryRepository adminProjectQueryRepository;
    private final EntityManager entityManager;
    private final FileRepository fileRepository;
    private final AnalysisRepository analysisRepository;
    private final OpinionRepository opinionRepository;
    private final ReportRepository reportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminProjectResponseDto> getProjects(AdminProjectSearchRequestDto request) {
        AdminPageResponseDto<AdminProjectResponseDto> page = adminProjectQueryRepository.search(request);
        log.info("[getProjects] 프로젝트 목록 조회: 전체 {}건, page={}", page.totalCount(), page.page());
        return page;
    }

    @Transactional(readOnly = true)
    public AdminProjectDetailResponseDto getProject(Long projectId) {
        AdminProjectResponseDto summary = adminProjectQueryRepository.findSummary(projectId)
                .orElseThrow(() -> {
                    log.warn("[getProject] 프로젝트 조회 실패: projectId={}", projectId);
                    return new ProjectNotFoundException();
                });

        return AdminProjectDetailResponseDto.builder()
                .id(summary.id())
                .title(summary.title())
                .status(summary.status())
                .opinionCollectionStatus(summary.opinionCollectionStatus())
                .inputType(summary.inputType())
                .userId(summary.userId())
                .userName(summary.userName())
                .userEmail(summary.userEmail())
                .createdAt(summary.createdAt())
                .updatedAt(summary.updatedAt())
                .deletedAt(summary.deletedAt())
                .usedCredit(summary.usedCredit())
                .averageScore(summary.averageScore())
                .file(adminProjectQueryRepository.findFile(projectId).orElse(null))
                .analysis(adminProjectQueryRepository.findAnalysis(projectId).orElse(null))
                .opinions(adminProjectQueryRepository.findOpinions(projectId))
                .report(adminProjectQueryRepository.findReport(projectId).orElse(null))
                .aiUsage(adminProjectQueryRepository.findAiUsage(projectId))
                .build();
    }

    @Transactional
    public void deleteProject(String adminEmail, Long projectId) {
        Project project = entityManager.find(Project.class, projectId, LockModeType.PESSIMISTIC_WRITE);
        if (project == null) {
            log.warn("[deleteProject] 프로젝트 조회 실패: projectId={}, 요청자={}", projectId, adminEmail);
            throw new ProjectNotFoundException();
        }

        Optional<File> file = fileRepository.findByProjectId(projectId);

        List<String> fileKeys = file
                .map(f -> Stream.of(f.getUrl(), f.getThumbnailUrl())
                        .filter(StringUtils::hasText)
                        .distinct()
                        .toList())
                .orElse(List.of());

        opinionRepository.deleteAll(opinionRepository.findAllByProjectIdWithDetails(projectId));
        reportRepository.findByProjectId(projectId).ifPresent(reportRepository::delete);
        analysisRepository.findByProjectId(projectId).ifPresent(analysisRepository::delete);
        file.ifPresent(fileRepository::delete);
        project.delete();

        eventPublisher.publishEvent(new ProjectDeletedEvent(projectId, fileKeys));

        log.info("[deleteProject] 관리자 프로젝트 삭제: projectId={}, 요청자={}", projectId, adminEmail);
    }
}
