package kr.yuns.dropthepitchserver.report.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    리포트 조회에 사용
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

//    리포트 전체 요약, 리포트 생성 전에는 비어 있음
    @Column(columnDefinition = "TEXT")
    private String summary;

//    리포트 생성 상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

//    연령별 의견 한 줄
    @Column
    private String insight;

//    내보내기 한 PDF 파일 경로
    @Column
    private String pdfUrl;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportItem> reportItems = new ArrayList<>();

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    public void addReportItem(ReportItem reportItem) {
        this.reportItems.add(reportItem);
    }

    public void completeReport(String summary, String insight) {
        this.insight = insight;
        this.summary = summary;
        this.status = ReportStatus.DONE;
    }

    public void assignPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
