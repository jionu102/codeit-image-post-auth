package codeit.sb06.imagepost.config;

import codeit.sb06.imagepost.entity.Member;
import codeit.sb06.imagepost.entity.Role;
import codeit.sb06.imagepost.repository.MemberRepository;
import codeit.sb06.imagepost.security.ApiInvalidSessionStrategy;
import codeit.sb06.imagepost.security.ApiSessionExpiredStrategy;
import codeit.sb06.imagepost.security.RestAuthenticationFailureHandler;
import codeit.sb06.imagepost.security.RestAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class securityConfig {
    private final ObjectMapper mapper;
    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())

                .rememberMe(remember -> remember
                        .key("server-key")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                        .rememberMeParameter("remember-me")

                        .tokenRepository(tokenRepository())
                        .userDetailsService(userDetailsService)
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler(new RestAuthenticationSuccessHandler(mapper))
                        .failureHandler(new RestAuthenticationFailureHandler(mapper))
                        .permitAll()
                ).
                sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        // 1. 세션 생성 정책: 필요 시 생성 (기본값이나 명시적으로 설정)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 2. 유효하지 않은 세션(만료 등) 접근 시 처리 전략
                        .invalidSessionStrategy(new ApiInvalidSessionStrategy(mapper))
                        // 3. 동시 세션 제어
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)                 // 최대 허용 세션 1개
                                .maxSessionsPreventsLogin(false)    // false: 기존 세션 만료 (밀어내기), true: 신규 로그인 차단
                                .expiredSessionStrategy(new ApiSessionExpiredStrategy(mapper))   // 세션 만료 시 이동할 URL (API 환경에서는 핸들러 처리가 더 적합할 수 있음)
                                .sessionRegistry(sessionRegistry())
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (memberRepository.findByUsername("user").isEmpty()) {
                memberRepository.save(Member.builder()
                        .username("user")
                        .password(passwordEncoder.encode("1234"))
                        .role(Role.USER)
                        .build());
            }
            if (memberRepository.findByUsername("admin").isEmpty()) {
                memberRepository.save(Member.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("1234"))
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }

    // [추가] 중복 세션 감지를 위한 session registry
    // 실제 세션의 메타데이터 저장소이자 세션을 관리하는 역할
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // [추가] 서블릿 세션 이벤트를 Spring Security로 연결하는 리스너
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }
}
