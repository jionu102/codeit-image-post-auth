package codeit.sb06.imagepost.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.InvalidSessionStrategy;

import java.io.IOException;
import java.util.Map;

public class ApiInvalidSessionStrategy implements InvalidSessionStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();

        // [/app] 경로로 시작하는 경우 예외 처리
        if (requestURI.startsWith("/app") || requestURI.startsWith("/api/debug")) {
            // 만료된 JSESSIONID 쿠키 삭제 (필수: 안 지우면 무한 루프 발생)
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            // 현재 요청한 페이지로 다시 리다이렉트 (쿠키 없이 재진입 -> Anonymous로 통과)
            response.sendRedirect(requestURI);
            return;
        }

        // 1. 상태 코드 401 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 2. JSON 에러 메시지 작성
        objectMapper.writeValue(response.getWriter(), Map.of(
                "code", "SESSION_EXPIRED",
                "message", "Current session is expired."
        ));
    }
}