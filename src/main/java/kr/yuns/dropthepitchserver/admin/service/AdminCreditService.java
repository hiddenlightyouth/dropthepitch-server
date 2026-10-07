package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminCreditHistorySearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminCreditSummaryResponseDto.TypeSummary;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminCreditQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminCreditService {
    private final AdminCreditQueryRepository adminCreditQueryRepository;

    @Transactional(readOnly = true)
    public AdminCreditSummaryResponseDto getSummary() {
        List<TypeSummary> byType = adminCreditQueryRepository.sumByType();

        long issued = byType.stream().mapToLong(TypeSummary::amount).filter(amount -> amount > 0).sum();
        long used = -byType.stream().mapToLong(TypeSummary::amount).filter(amount -> amount < 0).sum();
        long outstanding = adminCreditQueryRepository.sumOutstanding();

        log.info("[getSummary] 크레딧 합계 조회: 발행 {}, 사용 {}, 잔액 {}", issued, used, outstanding);

        return AdminCreditSummaryResponseDto.builder()
                .issued(issued)
                .used(used)
                .outstanding(outstanding)
                .byType(byType)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminCreditHistoryResponseDto> getHistory(
            AdminCreditHistorySearchRequestDto request) {
        AdminPageResponseDto<AdminCreditHistoryResponseDto> page = adminCreditQueryRepository.search(request);
        log.info("[getHistory] 크레딧 내역 조회: 전체 {}건, page={}", page.totalCount(), page.page());
        return page;
    }
}
