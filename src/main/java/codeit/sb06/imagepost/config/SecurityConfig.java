package codeit.sb06.imagepost.config;

import codeit.sb06.imagepost.entity.Member;
import codeit.sb06.imagepost.entity.Role;
import codeit.sb06.imagepost.repository.MemberRepository;
import codeit.sb06.imagepost.security.ApiInvalidSessionStrategy;
import codeit.sb06.imagepost.security.ApiSessionExpiredStrategy;
import codeit.sb06.imagepost.security.RestAuthenticationFailureHandler;
import codeit.sb06.imagepost.security.RestAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler(new RestAuthenticationSuccessHandler())
                        .failureHandler(new RestAuthenticationFailureHandler())
                        .permitAll()
                )
                // 세션 관리 정책 고도화
                .sessionManagement(session -> session
                        // 1. 세션 생성 정책: 필요 시 생성 (기본값이나 명시적으로 설정)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 2. [신규] 유효하지 않은 세션(만료 등) 접근 시 처리 전략
                        .invalidSessionStrategy(new ApiInvalidSessionStrategy())
                        // [Step 3-1] 세션 고정 보호 (신규 추가)
                        // 로그인 시 기존 세션 ID를 버리고 새로운 ID 발급
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        // [Step 3-2] 동시 세션 제어 고도화 (수정)
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .sessionRegistry(sessionRegistry())
                                // 기존: .expiredUrl("/app/login?expired")  <-- 삭제
                                // 변경: API 전용 핸들러 등록
                                .expiredSessionStrategy(new ApiSessionExpiredStrategy())
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                );

        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 단방향 암호화 객체 등록
    }

    @Bean
    public CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // admin 계정이 없으면 생성
            if (memberRepository.findByUsername("admin").isEmpty()) {
                Member admin = Member.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("1234")) // 비밀번호 암호화 필수
                        .role(Role.ADMIN)
                        .build();
                memberRepository.save(admin);
            }

            // user 계정이 없으면 생성
            if (memberRepository.findByUsername("user").isEmpty()) {
                Member user = Member.builder()
                        .username("user")
                        .password(passwordEncoder.encode("1234"))
                        .role(Role.USER)
                        .build();
                memberRepository.save(user);
            }
        };
    }
}
