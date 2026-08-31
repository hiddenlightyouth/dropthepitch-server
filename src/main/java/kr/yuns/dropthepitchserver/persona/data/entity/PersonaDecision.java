package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_decision")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaDecision extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    현재 가장 큰 불편
    @Column(nullable = false, columnDefinition = "TEXT")
    private String biggestPain;

//    이탈하거나 거부를 느끼는 트리거
    @Column(nullable = false, columnDefinition = "TEXT")
    private String churnTrigger;

//    페르소나 대표 소개글
    @Column(nullable = false, columnDefinition = "TEXT")
    private String signatureQuote;

//    선호하는 커뮤니케이션 스타일
    @Column(nullable = false, columnDefinition = "TEXT")
    private String communicationStyle;

//    주로 이동하는 교통(이동) 수단
    @Column(nullable = false, columnDefinition = "TEXT")
    private String transportation;

//    의심하거나 불신하는 포인트
    @Column(nullable = false, columnDefinition = "TEXT")
    private String distrustPoint;

//    서비스를 이용하는 데에 있어 무료 결제에서 유료 결제로 전환하는 기준
    @Column(nullable = false, columnDefinition = "TEXT")
    private String paymentTrigger;

//    의사결정에 영향을 주는 인물
    @Column(nullable = false)
    private String influencer;

//    주로 활동하는 커뮤니티
    @Column(nullable = false)
    private String community;

//    건강 상태
    @Column(nullable = false)
    private String healthStatus;
}
