package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminProjectQueryRepository;
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
}
