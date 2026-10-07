package kr.yuns.dropthepitchserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminPersonaSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPersonaResponseDto;
import kr.yuns.dropthepitchserver.admin.service.AdminPersonaService;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/tags")
    @Operation(summary = "페르소나 태그 목록 조회", description = "검색 조건에 쓸 태그 전체를 가나다순으로 돌려줍니다.")
    public GlobalResponse<List<String>> getTags() {
        return GlobalResponse.ok(adminPersonaService.getTags());
    }

    @GetMapping("/{personaId}")
    @Operation(summary = "페르소나 상세 조회", description = "전체 프로필과 최근 사용된 프로젝트를 함께 돌려줍니다.")
    public GlobalResponse<AdminPersonaDetailResponseDto> getPersona(@PathVariable Long personaId) {
        return GlobalResponse.ok(adminPersonaService.getPersona(personaId));
    }
}
