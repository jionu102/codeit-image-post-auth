package codeit.sb06.imagepost.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//세션 강제 만료에 따른 처리(중복 로그인)
@RequiredArgsConstructor
public class ApiSessionExpiredStrategy implements SessionInformationExpiredStrategy {
    private final ObjectMapper mapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        HttpServletResponse response = event.getResponse();
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        mapper.writeValue(response.getWriter(), Map.of(
                "code", "DUPLICATE_LOGIN",
                "message", "다른 기기에서 로그인하여 현재 세션이 만료되었습니다."
                )
        );
    }
}
