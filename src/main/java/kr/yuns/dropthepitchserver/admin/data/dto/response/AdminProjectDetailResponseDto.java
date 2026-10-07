package kr.yuns.dropthepitchserver.admin.data.dto.response;

import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisStatus;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisVerdict;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.opinion.data.enums.Sentiment;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import kr.yuns.dropthepitchserver.project.data.enums.OpinionCollectionStatus;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import kr.yuns.dropthepitchserver.report.data.enums.AgeGroup;
import kr.yuns.dropthepitchserver.report.data.enums.ReportItemType;
import kr.yuns.dropthepitchserver.report.data.enums.ReportStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminProjectDetailResponseDto(
        Long id,
        String title,
        ProjectStatus status,
        OpinionCollectionStatus opinionCollectionStatus,
        InputType inputType,
        Long userId,
        String userName,
        String userEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        long usedCredit,
        Double averageScore,
        FileInfo file,
        AnalysisInfo analysis,
        List<OpinionInfo> opinions,
        ReportInfo report,
        List<AdminAiPurposeUsageResponseDto> aiUsage
) {
    public record FileInfo(
            String name,
            InputType type,
            long size,
            String url,
            String thumbnailUrl
    ) { }

    public record AnalysisInfo(
            String uuid,
            AnalysisStatus status,
            AnalysisVerdict rejectReason,
            String content,
            List<String> selectedTags
    ) { }

    public record OpinionInfo(
            String uuid,
            Long personaId,
            String personaName,
            int personaAge,
            Gender personaGender,
            Double score,
            Sentiment sentiment,
            String summary
    ) { }

    public record ReportInfo(
            String uuid,
            ReportStatus status,
            String summary,
            String insight,
            List<ReportItemInfo> items
    ) { }

    public record ReportItemInfo(
            ReportItemType type,
            AgeGroup ageGroup,
            String content
    ) { }
}
