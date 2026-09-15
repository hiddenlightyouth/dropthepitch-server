package kr.yuns.dropthepitchserver.credit.data.repository;

import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository extends JpaRepository<Credit, Integer> {
}
