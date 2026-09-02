package kr.yuns.dropthepitchserver.analyze.service;

import kr.yuns.dropthepitchserver.analyze.data.dto.response.FileAnalyzeResponseDto;
import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.data.exception.AnalysisNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.exception.FileNotFoundException;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyzeService {
    private final AnalysisRepository analysisRepository;
    private final FileRepository fileRepository;

    private File GET_FILE_BY_PROJECT(Project project) {
        Optional<File> file = fileRepository.findByProject(project);

        if(file.isPresent()) {
            return file.get();
        } else {
            throw new FileNotFoundException();
        }
    }

    private Analysis GET_ANALYSIS_BY_PROJECT_ID(Long projectId, String email) {
        Optional<Analysis> analysis = analysisRepository.findByProject_IdAndProject_User_Email(projectId, email);

        if(analysis.isPresent()) {
            return analysis.get();
        } else {
            throw new AnalysisNotFoundException();
        }
    }

    @Transactional(readOnly = true)
    public FileAnalyzeResponseDto getProjectFileAnalyzeResult(String email, Long projectId) {
        Analysis analysis = GET_ANALYSIS_BY_PROJECT_ID(projectId, email);
        File file = GET_FILE_BY_PROJECT(analysis.getProject());

        List<FileAnalyzeResponseDto.VideoTimelineResponseDto> videoTimeline = null;

        if (file.getType() == InputType.MP4) {
            videoTimeline = analysis.getAnalysisTimelines().stream()
                    .map(analysisTimeline -> FileAnalyzeResponseDto.VideoTimelineResponseDto.builder()
                            .startTime(analysisTimeline.getStartTime())
                            .endTime(analysisTimeline.getEndTime())
                            .analyzeContent(analysisTimeline.getContent())
                            .build())
                    .toList();
        }

        return FileAnalyzeResponseDto.builder()
                .uuid(analysis.getUuid())
                .analysisStatus(analysis.getStatus())
                .type(file.getType())
                .downloadUrl(file.getUrl())
                .name(file.getName())
                .size(file.getSize())
                .thumbnailUrl(file.getThumbnailUrl())
                .analyzeContent(analysis.getContent())
                .videoTimeline(videoTimeline)
                .build();
    }
}
