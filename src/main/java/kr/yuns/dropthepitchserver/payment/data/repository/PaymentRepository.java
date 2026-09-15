package kr.yuns.dropthepitchserver.payment.data.repository;

import kr.yuns.dropthepitchserver.payment.data.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
