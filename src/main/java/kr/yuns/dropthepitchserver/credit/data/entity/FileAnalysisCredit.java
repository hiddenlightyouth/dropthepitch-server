package kr.yuns.dropthepitchserver.credit.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.*;

@Entity
@Table(name = "file_analysis_credit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class FileAnalysisCredit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_id", nullable = false)
    private Credit credit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Integer useCredit;

    public void use(Credit credit, Project project, Integer useCredit) {
        this.credit = credit;
        this.project = project;
        this.useCredit = useCredit;
        credit.decrease(useCredit);
    }
}
