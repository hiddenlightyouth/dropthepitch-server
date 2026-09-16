package kr.yuns.dropthepitchserver.credit.service;

import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditHistoryPageResponseDto;
import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditHistoryResponseDto;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryCategory;
import kr.yuns.dropthepitchserver.credit.data.enums.CreditHistoryType;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.FileAnalysisCreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.OpinionRequestCreditRepository;
import kr.yuns.dropthepitchserver.payment.data.repository.PaymentRepository;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
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

    //잔액을 계산하기 전 단계
    private record CreditChange(
            CreditHistoryType type,
            int amount,
            Long projectId,
            String projectTitle,
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

        List<CreditHistoryResponseDto> history = fillBalance(credit, collectChanges(credit.getId()));

        //잔액을 다 채운 뒤에 거름. 먼저 거르면 빠진 거래만큼 잔액이 어긋나는 현상 발생.
        List<CreditHistoryResponseDto> filtered = history.stream()
                .filter(item -> category == null || item.category() == category)
                .toList();

        log.info("[getHistory] 크레딧 내역 조회: email={}, 전체 {}건, category={}, page={}",
                email, filtered.size(), category, page);

        return toPage(filtered, page, size);
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

        fileAnalysisCreditRepository.findAllByCreditIdWithProject(creditId).forEach(used ->
                changes.add(toUsedChange(CreditHistoryType.FILE_ANALYSIS,
                        used.getUseCredit(), used.getProject(), used.getCreatedAt())));

        opinionRequestCreditRepository.findAllByCreditIdWithProject(creditId).forEach(used ->
                changes.add(toUsedChange(CreditHistoryType.OPINION_COLLECTION,
                        used.getUseCredit(), used.getProject(), used.getCreatedAt())));

        paymentRepository.findAllByCreditId(creditId).forEach(payment ->
                changes.add(new CreditChange(
                        CreditHistoryType.from(payment.getReason()),
                        payment.getAmount(),
                        null, null,
                        payment.getCreatedAt())));

        return changes;
    }

    //쓴 크레딧은 음수로
    private CreditChange toUsedChange(CreditHistoryType type, int useCredit,
                                      Project project, LocalDateTime occurredAt) {
        return new CreditChange(type, -useCredit, project.getId(), project.getTitle(), occurredAt);
    }

    //거래 직후 잔액은 저장 X. 현재 잔액에서 최신 거래부터 거꾸로 되짚어 채움.
    private List<CreditHistoryResponseDto> fillBalance(Credit credit, List<CreditChange> changes) {
        List<CreditHistoryResponseDto> history = new ArrayList<>();
        int running = credit.getAmount();

        for (CreditChange change : changes.stream()
                .sorted(Comparator.comparing(CreditChange::occurredAt).reversed())
                .toList()) {
            history.add(toResponse(change, running));
            running -= change.amount();
        }

        return history;
    }

    private CreditHistoryResponseDto toResponse(CreditChange change, int balance) {
        return CreditHistoryResponseDto.builder()
                .type(change.type())
                .typeDisplay(change.type().getDisplayName())
                .category(change.type().getCategory())
                .amount(change.amount())
                .balance(balance)
                .projectId(change.projectId())
                .projectTitle(change.projectTitle())
                .occurredAt(change.occurredAt())
                .build();
    }
}
