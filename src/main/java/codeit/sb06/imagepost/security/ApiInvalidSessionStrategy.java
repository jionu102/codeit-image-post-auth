package codeit.sb06.imagepost.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.session.InvalidSessionStrategy;

import java.io.IOException;
import java.util.Map;

//세션이 자연 소멸되거나 위조인 ID 일 경우 처리
@RequiredArgsConstructor
public class ApiInvalidSessionStrategy implements InvalidSessionStrategy {
    private final ObjectMapper mapper;


    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/app") || requestURI.startsWith("/api/debug")) {
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            response.sendRedirect(requestURI);
            return;
        }

        response.setStatus(401);
        response.setContentType("application/json");

        mapper.writeValue(response.getWriter(), Map.of(
                "code", "SESSION_EXPIRED",
                "message", "Current session expired"
        ));
    }
}
