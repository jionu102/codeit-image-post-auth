package codeit.sb06.imagepost.security.jwt;

import codeit.sb06.imagepost.entity.TokenInfo;
import codeit.sb06.imagepost.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> "REFRESH_TOKEN".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken != null) {
            try {
                // 토큰에서 username 추출 -> Member 조회 -> DB 삭제
                String username = jwtTokenProvider.getClaims(refreshToken).getSubject();
                memberRepository.findByUsername(username)
                        .ifPresent(member -> jwtRegistry.invalidateJwtInformationByUserId(member.getId()));
            } catch (Exception e) { /* 무시 */ }
        }

        response.addCookie(TokenUtil.emptyRefreshTokenCookie());
    }
}