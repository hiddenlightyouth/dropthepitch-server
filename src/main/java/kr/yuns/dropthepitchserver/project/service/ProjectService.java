package kr.yuns.dropthepitchserver.project.service;

import kr.yuns.dropthepitchserver.analyze.data.entity.Analysis;
import kr.yuns.dropthepitchserver.analyze.data.entity.File;
import kr.yuns.dropthepitchserver.analyze.data.enums.AnalysisStatus;
import kr.yuns.dropthepitchserver.analyze.data.enums.InputType;
import kr.yuns.dropthepitchserver.analyze.data.repository.AnalysisRepository;
import kr.yuns.dropthepitchserver.analyze.data.repository.FileRepository;
import kr.yuns.dropthepitchserver.analyze.event.ProjectCreatedEvent;
import kr.yuns.dropthepitchserver.common.s3.S3Service;
import kr.yuns.dropthepitchserver.project.data.dto.response.ProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.dto.response.SidebarProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.enums.ProjectStatus;
import kr.yuns.dropthepitchserver.project.data.exception.ProjectNotFoundException;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import kr.yuns.dropthepitchserver.report.data.entity.Report;
import kr.yuns.dropthepitchserver.report.data.repository.ReportRepository;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.data.exception.UserNotFoundException;
import kr.yuns.dropthepitchserver.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;
    private final ReportRepository reportRepository;
    private final AnalysisRepository analysisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;

    /**
     * 사용자 이메일로 User를 가져옵니다.
     *
     * @param email 이메일 주소
     * @return User
     */
    private User GET_USER_BY_EMAIL(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            log.error("[GET_USER_BY_EMAIL] 사용자 정보 조회 실패: {}", email);
            throw new UserNotFoundException();
        }
    }

    /**
     * 사이드바의 내 작업 목록을 조회합니다.
     *
     * @param email 이메일 주소
     * @return 프로젝트 ID, 프로젝트 명, 상태
     */
    public List<SidebarProjectResponseDto> getSidebarProject(String email) {
        User user = GET_USER_BY_EMAIL(email);
        List<Project> projectList = projectRepository.findAllByUserOrderByUpdatedAtDesc(user);
        log.info("[getSidebarProject] 사용자 사이드바 프로젝트 정보 조회: {}", email);
        return projectList.stream()
                .map(project -> SidebarProjectResponseDto.builder()
                        .projectId(project.getId())
                        .title(project.getTitle())
                        .status(project.getStatus())
                        .build())
                .toList();
    }
    private Project getProjectEntity(String email, Long projectId) {
        return projectRepository.findByIdAndUserEmail(projectId, email)
                .orElseThrow(() -> {
                    log.warn("[getProjectEntity] 프로젝트 조회 실패: projectId={}, email={}", projectId, email);
                    return new ProjectNotFoundException();
                });
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(String email, Long projectId) {
        Project project = getProjectEntity(email, projectId);

        ProjectResponseDto.FileDto file = fileRepository.findByProjectId(projectId)
                .map(f -> new ProjectResponseDto.FileDto(
                        f.getName(),
                        f.getSize(),
                        f.getType(),
                        //DB에는 S3 key가 들어있어 브라우저가 열 수 없다. 조회 시점에 임시 주소로 바꾼다.
                        s3Service.getDownloadUrl(f.getUrl()),
                        s3Service.getDownloadUrl(f.getThumbnailUrl())))
                .orElse(null);

        String reportId = reportRepository.findByProjectId(projectId)
                .map(Report::getUuid)
                .orElse(null);

        return new ProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getStatus(),
                project.getCreatedAt(),
                file,
                reportId);
    }

    /**
     * 파일을 업로드하여 새 작업(프로젝트)을 생성합니다.
     * 분석과 리포트 결과가 담길 레코드를 함께 생성합니다.
     *
     * @param email 사용자 이메일 주소
     * @param file 업로드할 파일
     * @return 프로젝트 기본 정보, 파일 정보, 리포트 ID
     */
    @Transactional
    public ProjectResponseDto createProject(String email, MultipartFile file) {
        User user = GET_USER_BY_EMAIL(email);
        String key = s3Service.upload(file);
        String originalFilename = file.getOriginalFilename();

        Project project = projectRepository.save(Project.builder()
                .user(user)
                .title(removeExtension(originalFilename))
                .status(ProjectStatus.IN_PROGRESS)
                .build());

        fileRepository.save(File.builder()
                .project(project)
                .type(resolveInputType(originalFilename))
                .url(key)
                .name(originalFilename)
                .size(Math.toIntExact(file.getSize()))
                .build());

        analysisRepository.save(Analysis.builder()
                .project(project)
                .status(AnalysisStatus.IN_PROGRESS)
                .build());

        reportRepository.save(Report.builder()
                .project(project)
                .build());

        //커밋이 끝난 뒤 분석이 시작되도록 이벤트만 발행한다. 실제 실행은 ProjectCreatedEventListener가 한다.
        eventPublisher.publishEvent(new ProjectCreatedEvent(project.getId()));

        log.info("[createProject] 새 작업 생성: projectId={}, email={}", project.getId(), email);

        return getProject(email, project.getId());
    }

    /**
     * 파일명에서 확장자를 제외한 이름을 반환합니다.
     *
     * @param originalFilename 원본 파일명
     * @return 확장자를 제외한 파일명
     */
    private String removeExtension(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        return dot > 0 ? originalFilename.substring(0, dot) : originalFilename;
    }

    /**
     * 파일명의 확장자로 파일 형식을 판별합니다.
     *
     * @param originalFilename 원본 파일명
     * @return 파일 형식
     */
    private InputType resolveInputType(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        return InputType.valueOf(extension.toUpperCase());
    }
}
