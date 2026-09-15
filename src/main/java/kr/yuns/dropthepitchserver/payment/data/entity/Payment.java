package kr.yuns.dropthepitchserver.payment.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.credit.data.entity.Credit;
import kr.yuns.dropthepitchserver.payment.data.enums.PaymentReason;
import lombok.*;

@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_id", nullable = false)
    private Credit credit;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentReason reason;

    public void addAmount(Credit credit, Integer amount, PaymentReason reason) {
        this.credit = credit;
        this.amount = amount;
        this.reason = reason;
        credit.increase(amount);
    }
}
