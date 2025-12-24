package codeit.sb06.imagepost.controller;

import codeit.sb06.imagepost.dto.JwtDto;
import codeit.sb06.imagepost.security.jwt.JwtInformation;
import codeit.sb06.imagepost.security.jwt.TokenUtil;
import codeit.sb06.imagepost.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response) {

        JwtInformation newInfo = authService.refreshToken(refreshToken);

        Cookie refreshCookie = TokenUtil.createRefreshTokenCookie(newInfo.getRefreshToken());
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(JwtDto.builder()
                .accessToken(newInfo.getAccessToken())
                .user(newInfo.getUserDto())
                .build());
    }
}