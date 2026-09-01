package kr.yuns.dropthepitchserver.project.service;

import kr.yuns.dropthepitchserver.project.data.dto.response.SidebarProjectResponseDto;
import kr.yuns.dropthepitchserver.project.data.entity.Project;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.data.exception.UserNotFoundException;
import kr.yuns.dropthepitchserver.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

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
}
