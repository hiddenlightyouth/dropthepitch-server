package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_economy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaEconomy extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    소득 또는 용돈 수준
    @Column(nullable = false)
    private String income;

//    자산 현황
    @Column(nullable = false)
    private String assets;

//    소비 습관
    @Column(nullable = false, columnDefinition = "TEXT")
    private String consumptionHabit;

//    최우선 가치 기준
    @Column(nullable = false, columnDefinition = "TEXT")
    private String coreValue;
}
