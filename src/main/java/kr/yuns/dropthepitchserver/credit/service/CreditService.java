package kr.yuns.dropthepitchserver.credit.service;

import kr.yuns.dropthepitchserver.credit.data.dto.response.CreditResponseDto;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
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
}
