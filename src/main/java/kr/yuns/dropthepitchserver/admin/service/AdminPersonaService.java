package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminPersonaSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminPersonaQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPersonaService {
    private final AdminPersonaQueryRepository adminPersonaQueryRepository;

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminPersonaResponseDto> getPersonas(AdminPersonaSearchRequestDto request) {
        AdminPageResponseDto<AdminPersonaResponseDto> page = adminPersonaQueryRepository.search(request);
        log.info("[getPersonas] 페르소나 목록 조회: 전체 {}명, page={}", page.totalCount(), page.page());
        return page;
    }
}
