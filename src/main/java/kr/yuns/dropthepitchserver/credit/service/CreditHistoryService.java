package kr.yuns.dropthepitchserver.credit.service;

import kr.yuns.dropthepitchserver.credit.data.dto.projection.CreditUseView;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditHistoryPageResponseDto;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.FileAnalysisCreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.OpinionRequestCreditRepository;
import kr.yuns.dropthepitchserver.payment.data.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//크레딧 내역 조회만 담당한다. 조회에 리포지토리 네 개가 필요해 CreditService와 나눠 뒀다.
@Service
@Slf4j
@RequiredArgsConstructor
public class CreditHistoryService {

    private final CreditRepository creditRepository;
    private final FileAnalysisCreditRepository fileAnalysisCreditRepository;
    private final OpinionRequestCreditRepository opinionRequestCreditRepository;
    private final PaymentRepository paymentRepository;

    //세 테이블에 흩어진 변동 내역을 한 줄로 맞춰 담는다.
    private record CreditChange(
            CreditHistoryType type,
            int amount,
            Long projectId,
            String projectTitle,
            boolean projectDeleted,
            LocalDateTime occurredAt
    ) { }

    /**
     * 크레딧 사용, 충전, 적립 내역을 최신순으로 조회.
     */
    @Transactional(readOnly = true)
    public CreditHistoryPageResponseDto getHistory(String email, CreditHistoryCategory category,
                                                   int page, int size) {
        Credit credit = creditRepository.findByUserEmail(email).orElse(null);

        //크레딧 정보가 없으면 쌓인 내역도 없다.
        if (credit == null) {
            log.info("[getHistory] 크레딧 정보가 없어 빈 목록을 반환합니다: email={}", email);
            return toPage(List.of(), page, size);
        }

        List<CreditHistoryResponseDto> history = collectChanges(credit.getId()).stream()
                .sorted(Comparator.comparing(CreditChange::occurredAt).reversed())
                .filter(change -> category == null || change.type().getCategory() == category)
                .map(this::toResponse)
                .toList();

        log.info("[getHistory] 크레딧 내역 조회: email={}, 전체 {}건, category={}, page={}",
                email, history.size(), category, page);

        return toPage(history, page, size);
    }

    //세 테이블을 시간순으로 합쳐야 해서 전부 읽은 뒤 자른다. 조회량이 문제가 되면 UNION 쿼리로 옮기는게 좋아보임
    private CreditHistoryPageResponseDto toPage(List<CreditHistoryResponseDto> all, int page, int size) {
        List<CreditHistoryResponseDto> items = all.stream()
                .skip((long) page * size)
                .limit(size)
                .toList();

        int totalPages = (int) Math.ceil((double) all.size() / size);

        return CreditHistoryPageResponseDto.builder()
                .items(items)
                .page(page)
                .size(size)
                .totalCount(all.size())
                .totalPages(totalPages)
                .hasNext((long) (page + 1) * size < all.size())
                .build();
    }

    //세 테이블에 나뉜 변동 내역을 한 목록으로 모은다.
    private List<CreditChange> collectChanges(Long creditId) {
        List<CreditChange> changes = new ArrayList<>();

        fileAnalysisCreditRepository.findAllUsedByCreditId(creditId).forEach(used ->
                changes.add(toUsedChange(CreditHistoryType.FILE_ANALYSIS, used)));

        opinionRequestCreditRepository.findAllUsedByCreditId(creditId).forEach(used ->
                changes.add(toUsedChange(CreditHistoryType.OPINION_COLLECTION, used)));

        //충전과 적립은 프로젝트와 무관하다.
        paymentRepository.findAllByCreditId(creditId).forEach(payment ->
                changes.add(new CreditChange(
                        CreditHistoryType.from(payment.getReason()),
                        payment.getAmount(),
                        null, null, false,
                        payment.getCreatedAt())));

        return changes;
    }

    //쓴 크레딧은 음수로
    private CreditChange toUsedChange(CreditHistoryType type, CreditUseView used) {
        return new CreditChange(
                type,
                -used.getUseCredit(),
                used.getProjectId(),
                used.getProjectTitle(),
                used.getProjectDeletedAt() != null,
                used.getOccurredAt());
    }

    private CreditHistoryResponseDto toResponse(CreditChange change) {
        return CreditHistoryResponseDto.builder()
                .type(change.type())
                .typeDisplay(change.type().getDisplayName())
                .category(change.type().getCategory())
                .amount(change.amount())
                .projectId(change.projectId())
                .projectTitle(change.projectTitle())
                .projectDeleted(change.projectDeleted())
                .occurredAt(change.occurredAt())
                .build();
    }
}
