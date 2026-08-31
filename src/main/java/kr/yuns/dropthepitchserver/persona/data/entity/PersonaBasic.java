package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_basic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaBasic extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    국적
    @Column(nullable = false)
    private String nationality;

//    학력
    @Column(nullable = false)
    private String education;

//    문과, 이과 구분
    @Column(nullable = false)
    private String academicTrack;

//    거주지, 주거 형태
    @Column(nullable = false)
    private String residence;

//    직업 또는 장래희망
    @Column(nullable = false)
    private String job;

//    군필 여부
    @Column(nullable = false)
    private String militaryService;

//    가족 관계
    @Column(nullable = false)
    private String family;

//    연애 유무
    @Column(nullable = false)
    private String relationshipStatus;
}
