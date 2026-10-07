package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminProjectSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminProjectResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminProjectService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
    private final AdminProjectService adminProjectService;

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회",
            description = "제목, 파일 이름, 사용자 검색과 상태, 의견 수집 상태, 파일 형식, 거절 사유, 만든 날, 평균 점수 범위, 삭제 여부로 거르고 정렬합니다.")
    public GlobalResponse<AdminPageResponseDto<AdminProjectResponseDto>> getProjects(
            @ModelAttribute AdminProjectSearchRequestDto request) {
        return GlobalResponse.ok(adminProjectService.getProjects(request));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 상세 조회",
            description = "업로드 파일, 파일 분석 결과, 페르소나 의견, 리포트, 용도별 AI 사용량을 함께 돌려줍니다. 삭제된 프로젝트도 조회됩니다.")
    public GlobalResponse<AdminProjectDetailResponseDto> getProject(@PathVariable Long projectId) {
        return GlobalResponse.ok(adminProjectService.getProject(projectId));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제",
            description = "사용자가 직접 지울 때와 같이 처리합니다. AI 사용량과 크레딧 내역은 남습니다.")
    public GlobalResponse<Void> deleteProject(@PathVariable Long projectId) {
        adminProjectService.deleteProject(SecurityUtil.getUsername(), projectId);
        return GlobalResponse.ok();
    }
}
