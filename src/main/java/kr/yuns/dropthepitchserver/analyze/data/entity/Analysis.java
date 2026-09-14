package kr.yuns.dropthepitchserver.analyze.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisStatus;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Analysis extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String detail;

    //페르소나 선별에 사용한 태그(콤마 구분). 페르소나를 교체시 필요
    @Column(columnDefinition = "TEXT")
    private String selectedTags;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    private AnalysisVerdict rejectReason;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    @Builder.Default
    private List<AnalysisTimeline> analysisTimelines = new ArrayList<>();

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    public void addAnalysisTimeline(AnalysisTimeline analysisTimeline) {
        this.analysisTimelines.add(analysisTimeline);
    }

    /**
     * 분석이 끝났을 때 결과를 채우고 상태를 완료로 바꿉니다.
     *
     * @param content 화면에 표시되는 한 줄 요약
     * @param detail 다음 단계에서 사용할 분석 상세 결과(JSON)
     */
    public void complete(String content, String detail) {
        this.content = content;
        this.detail = detail;
        this.status = AnalysisStatus.COMPLETED;
    }

    /**
     * 분석이 실패했을 때 상태만 실패로 바꿉니다.
     * 화면이 계속 '분석중'에 머물지 않게 하기 위함입니다.
     */
    public void fail(AnalysisVerdict reason) {
        this.status = AnalysisStatus.FAILED;
        this.rejectReason = reason;
    }


    public void saveSelectedTags(List<String> selectedTags) {
        this.selectedTags = String.join(",", selectedTags);
    }

    //저장해둔 선별 태그를 목록으로(,으로 구분).
    public List<String> getSelectedTagList() {
        if (selectedTags == null || selectedTags.isBlank()) {
            return List.of();
        }
        return List.of(selectedTags.split(","));
    }
}
