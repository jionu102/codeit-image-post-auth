package codeit.sb06.imagepost.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/api/debug/context")
    public String getContextInfo() {
        // 현재 스레드에 저장된 인증 객체 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "Current Context: Anonymous (No Authentication)";
        }

        return "Current Context: " + authentication.getName() + " (" + authentication.getAuthorities() + ")";
    }
}
