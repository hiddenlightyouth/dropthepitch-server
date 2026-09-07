package kr.yuns.dropthepitchserver.project.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import lombok.*;

@Entity
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OpinionCollectionStatus opinionCollectionStatus = OpinionCollectionStatus.NOT_STARTED;

    //업로드 시점에는 파일명이 들어가고, 분석이 끝나면 AI가 지은 제목으로 바뀐다.
    public void changeTitle(String title) {
        this.title = title;
    }
}
