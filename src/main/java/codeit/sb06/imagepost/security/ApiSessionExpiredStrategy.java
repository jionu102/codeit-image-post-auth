package codeit.sb06.imagepost.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletResponse response = event.getResponse();

        // 1. 응답 설정: JSON 타입 & 401 Unauthorized
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 2. 메시지 작성
        objectMapper.writeValue(response.getWriter(), Map.of(
                "code", "DUPLICATE_LOGIN",
                "message", "다른 기기에서 로그인하여 현재 세션이 만료되었습니다."
        ));
    }
}