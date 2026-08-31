package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_psychology")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaPsychology extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    성격
    @Column(nullable = false, columnDefinition = "TEXT")
    private String personality;

//    MBTI
    @Column(nullable = false)
    private String mbti;

//    가치관
    @Column(nullable = false, columnDefinition = "TEXT")
    private String belief;

//    위험 감수 성향
    @Column(nullable = false, columnDefinition = "TEXT")
    private String riskTolerance;

//    정치 성향
    @Column(nullable = false)
    private String directionPreference;

//    다른 사람에게 서비스를 추천하거나, 공유하는 성향
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sharingTendency;
}
