package codeit.sb06.imagepost.config;

import codeit.sb06.imagepost.entity.Member;
import codeit.sb06.imagepost.entity.Role;
import codeit.sb06.imagepost.repository.MemberRepository;
import codeit.sb06.imagepost.security.ApiInvalidSessionStrategy;
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
                        // 3. 동시 세션 제어
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)                 // 최대 허용 세션 1개
                                .maxSessionsPreventsLogin(false)    // false: 기존 세션 만료(밀어내기)
                                .expiredUrl("/app/login?expired")   // 세션 만료 시 이동 URL
                                .sessionRegistry(sessionRegistry()) // 레지스트리 등록

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
