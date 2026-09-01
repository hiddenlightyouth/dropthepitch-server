package kr.yuns.dropthepitchserver.report.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportItemType;
import lombok.*;

@Entity
@Table(name = "report_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class ReportItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

//    긍정, 부정, 연령대별 항목 구분
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportItemType type;

//    연령대별 항목이 아닌 경우 비어 있음
    @Column
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

//    항목 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
