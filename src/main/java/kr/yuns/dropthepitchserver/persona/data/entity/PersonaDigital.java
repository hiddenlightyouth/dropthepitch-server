package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_digital")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaDigital extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    어떤 스마트폰(iOS, Android)
    @Column(nullable = false)
    private String mobileOs;

//    AI 이해도
    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiLiteracy;

//    신기술에 대한 반응
    @Column(nullable = false, columnDefinition = "TEXT")
    private String newTechAttitude;

//    정보를 습득하거나 검증하는 경로
    @Column(nullable = false, columnDefinition = "TEXT")
    private String infoSource;
}
