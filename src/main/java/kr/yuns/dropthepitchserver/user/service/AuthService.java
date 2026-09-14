package kr.yuns.dropthepitchserver.user.service;

import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.AuthenticationToken;
import kr.yuns.dropthepitchserver.common.security.JwtTokenProvider;
import kr.yuns.dropthepitchserver.common.security.exception.TokenInvalidException;
import kr.yuns.dropthepitchserver.user.data.dto.request.RefreshRequestDto;
import kr.yuns.dropthepitchserver.user.data.dto.request.SignInRequestDto;
import kr.yuns.dropthepitchserver.user.data.dto.request.SignUpRequestDto;
import kr.yuns.dropthepitchserver.user.data.dto.response.TokenResponseDto;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.data.exception.EmailDuplicationException;
import kr.yuns.dropthepitchserver.user.data.exception.PasswordInvalidException;
import kr.yuns.dropthepitchserver.user.data.exception.UserNotFoundException;
import kr.yuns.dropthepitchserver.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public User getUserEntity(String email) {
        log.info("[getUserEntity] 사용자 조회 시도: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[getUserEntity] 사용자 조회 실패: {}", email);
                    return new UserNotFoundException();
                });
    }

    private void validatePassword(String originalPassword, String password) {
        if(!passwordEncoder.matches(originalPassword, password)) {
            throw new PasswordInvalidException();
        }
    }

    private Authentication createAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getValue())
        );

        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
    }

    @Transactional
    public GlobalResponse<TokenResponseDto> signUp(SignUpRequestDto signUpRequestDto) {
        User user = User.builder()
                .email(signUpRequestDto.getEmail())
                .name(signUpRequestDto.getName())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .registeredAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
            log.info("[signUp] 새로운 사용자 등록: {}", user.getEmail());
        } catch (DataIntegrityViolationException e) {
            log.error("[signUp] 중복된 이메일 주소로 인한 가입 거부: {}", signUpRequestDto.getEmail());
            throw new EmailDuplicationException();
        }

        Authentication authentication = createAuthentication(user);
        AuthenticationToken authenticationToken = tokenProvider.generateToken(authentication);

        return GlobalResponse.ok(
                TokenResponseDto.builder()
                        .name(user.getName())
                        .accessToken(authenticationToken.getAccessToken())
                        .refreshToken(authenticationToken.getRefreshToken())
                        .build());
    }

    public GlobalResponse<TokenResponseDto> signIn(SignInRequestDto signInRequestDto) {
        User user = getUserEntity(signInRequestDto.getEmail());
        validatePassword(signInRequestDto.getPassword(), user.getPassword());

        Authentication authentication = createAuthentication(user);
        AuthenticationToken authenticationToken = tokenProvider.generateToken(authentication);

        return GlobalResponse.ok(
                TokenResponseDto.builder()
                        .name(user.getName())
                        .accessToken(authenticationToken.getAccessToken())
                        .refreshToken(authenticationToken.getRefreshToken())
                        .build());
    }

    public GlobalResponse<TokenResponseDto> refresh(String bearerToken, RefreshRequestDto refreshRequestDto) {
        String accessToken = tokenProvider.resolveToken(bearerToken);
        String refreshToken = refreshRequestDto.getRefreshToken();

        if (accessToken == null || !tokenProvider.validateTokenAllowExpired(accessToken)) {
            log.error("[refresh] 유효하지 않은 Access Token으로 재발급 시도");
            throw new TokenInvalidException();
        }

        if (!tokenProvider.validateToken(refreshToken)) {
            log.error("[refresh] 유효하지 않은 Refresh Token으로 재발급 시도");
            throw new TokenInvalidException();
        }

        String email = tokenProvider.getSubject(refreshToken);

        if (!email.equals(tokenProvider.getSubject(accessToken))) {
            log.error("[refresh] 소유자가 일치하지 않는 토큰으로 재발급 시도: {}", email);
            throw new TokenInvalidException();
        }

        if (!tokenProvider.isStoredRefreshToken(email, refreshToken)) {
            log.error("[refresh] 저장된 Refresh Token과 일치하지 않는 재발급 시도: {}", email);
            throw new TokenInvalidException();
        }

        User user = getUserEntity(email);
        Authentication authentication = createAuthentication(user);

        tokenProvider.blacklistAccessToken(accessToken);
        tokenProvider.blacklistRefreshToken(refreshToken);
        AuthenticationToken authenticationToken = tokenProvider.generateToken(authentication);
        log.info("[refresh] 토큰 재발급 완료: {}", email);

        return GlobalResponse.ok(
                TokenResponseDto.builder()
                        .name(user.getName())
                        .accessToken(authenticationToken.getAccessToken())
                        .refreshToken(authenticationToken.getRefreshToken())
                        .build());
    }

    public GlobalResponse<Void> logout(String bearerToken) {
        String accessToken = tokenProvider.resolveToken(bearerToken);

        if (accessToken == null || !tokenProvider.validateToken(accessToken)) {
            log.error("[logout] 유효하지 않은 Access Token으로 로그아웃 시도");
            throw new TokenInvalidException();
        }

        tokenProvider.invalidateToken(accessToken);
        log.info("[logout] 로그아웃 처리 완료");

        return GlobalResponse.ok();
    }
}
