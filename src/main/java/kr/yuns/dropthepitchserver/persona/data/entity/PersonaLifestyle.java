package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import lombok.*;

@Entity
@Table(name = "persona_lifestyle")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class PersonaLifestyle extends BaseEntity {
    @Id
    private Long personaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id")
    private Persona persona;

//    취미
    @Column(nullable = false)
    private String hobby;

//    운동 유, 무
    @Column(nullable = false)
    private String exercise;

//    밤낮(수면) 패턴
    @Column(nullable = false)
    private String dayNightPattern;

//    해외 여행 경험
    @Column(nullable = false)
    private String overseasTravel;

//    집중력 지속 시간
    @Column(nullable = false)
    private String attentionSpan;
}
