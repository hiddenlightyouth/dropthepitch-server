package kr.yuns.dropthepitchserver.payment.data.repository;

import kr.yuns.dropthepitchserver.payment.data.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    //충전과 적립 내역
    List<Payment> findAllByCreditId(Long creditId);
}
