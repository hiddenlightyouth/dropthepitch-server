package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminProjectQueryRepository;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProjectService {
    private final AdminProjectQueryRepository adminProjectQueryRepository;

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
}
