package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminPersonaSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminPersonaService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/personas")
@RequiredArgsConstructor
public class AdminPersonaController {
    private final AdminPersonaService adminPersonaService;

    @GetMapping
    @Operation(summary = "페르소나 목록 조회",
            description = "이름, 직업, 태그, 소개글 검색과 연령대, 성별, MBTI, 태그, 사용 여부로 거르고 정렬합니다. 사용 횟수, 평균 점수, 최근 사용일을 함께 돌려줍니다.")
    public GlobalResponse<AdminPageResponseDto<AdminPersonaResponseDto>> getPersonas(
            @ModelAttribute AdminPersonaSearchRequestDto request) {
        return GlobalResponse.ok(adminPersonaService.getPersonas(request));
    }
}
