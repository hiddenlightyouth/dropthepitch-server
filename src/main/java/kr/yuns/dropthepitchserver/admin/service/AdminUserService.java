package kr.yuns.dropthepitchserver.admin.service;

import kr.yuns.dropthepitchserver.admin.data.dto.response.AdminMeResponseDto;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.data.exception.UserNotFoundException;
import kr.yuns.dropthepitchserver.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminMeResponseDto getMe(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return AdminMeResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
