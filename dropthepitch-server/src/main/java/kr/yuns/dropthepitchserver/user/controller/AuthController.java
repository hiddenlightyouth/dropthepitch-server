package kr.yuns.dropthepitchserver.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.user.data.dto.request.SignInRequestDto;
import kr.yuns.dropthepitchserver.user.data.dto.request.SignUpRequestDto;
import kr.yuns.dropthepitchserver.user.data.dto.response.TokenResponseDto;
import kr.yuns.dropthepitchserver.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "사용자 회원가입")
    public GlobalResponse<TokenResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        return authService.signUp(signUpRequestDto);
    }

    @PostMapping("/signin")
    @Operation(summary = "사용자 로그인")
    public GlobalResponse<TokenResponseDto> signIn(@Valid @RequestBody SignInRequestDto signInRequestDto) {
        return authService.signIn(signInRequestDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃")
    public GlobalResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return authService.logout(bearerToken);
    }
}
