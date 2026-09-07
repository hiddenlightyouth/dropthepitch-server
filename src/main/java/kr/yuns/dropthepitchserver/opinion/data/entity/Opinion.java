package kr.yuns.dropthepitchserver.opinion.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "opinion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Opinion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    의견 조회에 사용
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

//    감정 점수, 의견 수집 전에는 비어 있음
    @Column
    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

//    한 줄 코멘트, 의견 수집 전에는 비어 있음
    @Column
    private String summary;

    @OneToMany(mappedBy = "opinion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpinionDetail> opinionDetails = new ArrayList<>();

    //Analysis, Report와 동일하게 저장 직전에 uuid를 채운다. 없으면 not null 위반으로 저장이 실패한다.
    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    public void addOpinionDetail(OpinionDetail opinionDetail) {
        this.opinionDetails.add(opinionDetail);
    }

    public void updateResult(Sentiment sentiment, String summary) {
        this.sentiment = sentiment;
        this.summary = summary;
    }
    //페르소나 변경
    public void replacePersona(Persona persona) {
        this.persona = persona;
    }
}
