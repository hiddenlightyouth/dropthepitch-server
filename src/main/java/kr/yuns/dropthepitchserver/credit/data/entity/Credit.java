package kr.yuns.dropthepitchserver.credit.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.credit.data.exception.InsufficientCreditException;
import kr.yuns.dropthepitchserver.credit.data.exception.InvalidCreditAmountException;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import lombok.*;

@Entity
@Table(name = "credit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Credit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer amount = 0;

    public void increase(int value) {
        validateValue(value);
        this.amount += value;
    }

    public void decrease(int value) {
        validateValue(value);
        if (this.amount < value) {
            throw new InsufficientCreditException();
        }
        this.amount -= value;
    }

    public void validateValue(int value) {
        if (value <= 0) {
            throw new InvalidCreditAmountException();
        }
    }
}
