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

    /**
     * File을 Project로 조회합니다.
     *
     * @param project Project
     * @return File
     */
    private File GET_FILE_BY_PROJECT(Project project) {
        Optional<File> file = fileRepository.findByProject(project);

        if(file.isPresent()) {
            return file.get();
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * Analysis를 프로젝트 ID로 조회합니다.
     * 사용자가 생성한 프로젝트만 조회할 수 있습니다.
     *
     * @param projectId 프로젝트 ID
     * @param email 사용자 이메일 주소
     * @return Analysis
     */
    private Analysis GET_ANALYSIS_BY_PROJECT_ID(Long projectId, String email) {
        Optional<Analysis> analysis = analysisRepository.findByProject_IdAndProject_User_Email(projectId, email);

        if(analysis.isPresent()) {
            return analysis.get();
        } else {
            throw new AnalysisNotFoundException();
        }
    }

    /**
     * 프로젝트의 파일 분석 결과를 조회합니다.
     * @param email 사용자 이메일 주소
     * @param projectId 프로젝트 ID
     * @return 파일 기본 정보, 분석 결과
     */
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
