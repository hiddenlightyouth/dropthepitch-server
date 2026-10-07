package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminCreditGrantRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminRoleChangeRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.request.AdminUserSearchRequestDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminMeResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminPageResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserDetailResponseDto;
import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminUserResponseDto;
import kr.yuns.dropthepitchserver.admin.data.exception.AdminSelfRoleChangeException;
import kr.yuns.dropthepitchserver.admin.data.repository.AdminUserQueryRepository;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.credit.data.exception.InvalidCreditAmountException;
import kr.yuns.dropthepitchserver.credit.data.repository.CreditRepository;
import kr.yuns.dropthepitchserver.payment.data.entity.Payment;
import kr.yuns.dropthepitchserver.payment.data.enums.PaymentReason;
import kr.yuns.dropthepitchserver.payment.data.repository.PaymentRepository;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.data.exception.UserNotFoundException;
import kr.yuns.dropthepitchserver.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {
    private final AdminUserQueryRepository adminUserQueryRepository;
    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public AdminMeResponseDto getMe(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return AdminMeResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminPageResponseDto<AdminUserResponseDto> getUsers(AdminUserSearchRequestDto request) {
        AdminPageResponseDto<AdminUserResponseDto> page = adminUserQueryRepository.search(request);
        log.info("[getUsers] 사용자 목록 조회: 전체 {}명, page={}", page.totalCount(), page.page());
        return page;
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponseDto getUser(Long userId) {
        return adminUserQueryRepository.findDetail(userId)
                .orElseThrow(() -> {
                    log.warn("[getUser] 사용자 조회 실패: userId={}", userId);
                    return new UserNotFoundException();
                });
    }

    @Transactional
    public void changeRole(String adminEmail, Long userId, AdminRoleChangeRequestDto request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getEmail().equals(adminEmail)) {
            log.warn("[changeRole] 자신의 역할 변경 시도: {}", adminEmail);
            throw new AdminSelfRoleChangeException();
        }

        user.setRole(request.getRole());
        log.info("[changeRole] 역할 변경: userId={}, role={}, 요청자={}", userId, request.getRole(), adminEmail);
    }

    @Transactional
    public void grantCredit(String adminEmail, Long userId, AdminCreditGrantRequestDto request) {
        int amount = request.getAmount();
        if (amount <= 0) {
            throw new InvalidCreditAmountException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Credit credit = creditRepository.findWithLockByUserEmail(user.getEmail())
                .orElseGet(() -> creditRepository.save(Credit.builder().user(user).build()));

        Payment payment = Payment.builder().build();
        payment.addAmount(credit, amount, PaymentReason.ADMIN_GRANT);
        paymentRepository.save(payment);

        log.info("[grantCredit] 관리자 크레딧 지급: userId={}, 지급 {}, 잔액 {}, 요청자={}",
                userId, amount, credit.getAmount(), adminEmail);
    }
}
