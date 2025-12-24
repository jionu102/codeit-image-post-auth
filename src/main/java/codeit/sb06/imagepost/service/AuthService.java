package codeit.sb06.imagepost.service;

import codeit.sb06.imagepost.dto.UserDto;
import codeit.sb06.imagepost.entity.Member;
import codeit.sb06.imagepost.exception.InvalidRefreshTokenException;
import codeit.sb06.imagepost.repository.MemberRepository;
import codeit.sb06.imagepost.security.jwt.JwtInformation;
import codeit.sb06.imagepost.security.jwt.JwtRegistry;
import codeit.sb06.imagepost.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final MemberRepository memberRepository;

    @Transactional
    public JwtInformation refreshToken(String refreshToken) {
        // 1. 검증: 서명 유효성 확인 AND DB 존재 여부 확인
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken) ||
                !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        // 2. 정보 조회
        String username = jwtTokenProvider.getClaims(refreshToken).getSubject();
        Member member = memberRepository.findByUsername(username).orElseThrow();

        // 3. 새 토큰 생성
        String newAccess = jwtTokenProvider.createAccessToken(username, member.getRole().name());
        String newRefresh = jwtTokenProvider.createRefreshToken(username, member.getRole().name());

        // 4. DB Rotation
        JwtInformation newInfo = new JwtInformation(UserDto.from(member), newAccess, newRefresh);
        jwtRegistry.rotateJwtInformation(refreshToken, newInfo);

        return newInfo;
    }
}
