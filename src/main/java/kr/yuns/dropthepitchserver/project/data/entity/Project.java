package kr.yuns.dropthepitchserver.project.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
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

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private File file;

    public void assignFile(File file) {
        this.file = file;
    }
}
