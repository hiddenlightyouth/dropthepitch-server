package kr.yuns.dropthepitchserver.credit.service;

import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditResponseDto;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.exception.InsufficientCreditException;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
import kr.yuns.dropthepitchserver.credit.data.repository.FileAnalysisCreditRepository;
import kr.yuns.dropthepitchserver.payment.data.entity.Payment;
import kr.yuns.dropthepitchserver.payment.data.enums.PaymentReason;
import kr.yuns.dropthepitchserver.payment.data.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditService {
    private static final int NO_CREDIT = 0;

    private final CreditRepository creditRepository;
    private final FileAnalysisCreditRepository fileAnalysisCreditRepository;
    private final PaymentRepository paymentRepository;

    /**사용자가 보유한 크레딧을 조회*/
    @Transactional(readOnly = true)
    public CreditResponseDto getCredit(String email) {
        int amount = creditRepository.findByUserEmail(email)
                .map(Credit::getAmount)
                .orElse(NO_CREDIT); //크레딧 정보가 없는  사용자 0으로 간주
        log.info("[getCredit] 보유 크레딧 조회: email={}, amount={}", email, amount);
        return CreditResponseDto.builder()
                .amount(amount)
                .build();
    }

    /**
     * 분석이 거절되거나 실패한 새 작업의 크레딧을 돌려줍니다.
     *
     * @param projectId 프로젝트 ID
     * @param keepCredit 돌려주지 않고 남길 크레딧
     */
    @Transactional
    public void refundFileAnalysis(Long projectId, int keepCredit) {
        fileAnalysisCreditRepository.findByProjectId(projectId).ifPresent(used -> {
            int refund = used.getUseCredit() - keepCredit;

            if (refund <= 0) {
                return;
            }

            Credit credit = creditRepository.findWithLockById(used.getCredit().getId())
                    .orElseThrow(InsufficientCreditException::new);

            Payment payment = Payment.builder().build();
            payment.addAmount(credit, refund, PaymentReason.REFUND);
            paymentRepository.save(payment);

            log.info("[refundFileAnalysis] 크레딧 환불: projectId={}, 환불 {}, 잔액 {}",
                    projectId, refund, credit.getAmount());
        });
    }
}
