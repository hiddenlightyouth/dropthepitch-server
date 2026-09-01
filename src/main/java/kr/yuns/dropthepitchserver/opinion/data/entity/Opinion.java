package kr.yuns.dropthepitchserver.opinion.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.entity.Persona;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    public void addOpinionDetail(OpinionDetail opinionDetail) {
        this.opinionDetails.add(opinionDetail);
    }

    public void updateResult(Sentiment sentiment, String summary) {
        this.sentiment = sentiment;
        this.summary = summary;
    }
}
