package kr.yuns.dropthepitchserver.credit.data.repository;

import jakarta.persistence.LockModeType;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Credit> findWithLockByUserEmail(String email);
}
