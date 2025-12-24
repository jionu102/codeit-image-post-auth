package codeit.sb06.imagepost.security;

import codeit.sb06.imagepost.dto.JwtDto;
import codeit.sb06.imagepost.dto.UserDto;
import codeit.sb06.imagepost.entity.Member;
import codeit.sb06.imagepost.repository.MemberRepository;
import codeit.sb06.imagepost.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 인증된 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. 토큰 생성
        // Access Token: 유효기간 짧음 (예: 1시간)
        // Refresh Token: 유효기간 긺 (예: 2주), Access Token 재발급용
        String accessToken = jwtTokenProvider.generateToken(member.getUsername(), member.getRole().name());
        String refreshToken = jwtTokenProvider.generateToken(member.getUsername(), member.getRole().name()); // 편의상 동일 로직 사용, 실무에선 만료시간 다르게 설정 권장

        // 3. Refresh Token을 쿠키에 저장
        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true); // JS에서 접근 불가
        refreshCookie.setPath("/");      // 모든 경로에서 전송
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        // refreshCookie.setSecure(true); // HTTPS 적용 시 필수 해제

        response.addCookie(refreshCookie);

        // 4. 응답 작성 (Access Token + User Info)
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        JwtDto jwtDto = JwtDto.builder()
                .accessToken(accessToken)
                .user(UserDto.from(member))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    }
}