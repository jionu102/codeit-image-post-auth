package codeit.sb06.imagepost.security.jwt;

import codeit.sb06.imagepost.entity.TokenInfo;
import codeit.sb06.imagepost.repository.TokenInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class PersistentJwtRegistry implements JwtRegistry {

    private final TokenInfoRepository tokenInfoRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        Long userId = jwtInformation.getUserDto().getId();

        // 동시 로그인 제한: 기존 토큰 무조건 삭제 (Max 1)
        tokenInfoRepository.deleteByUserId(userId);
        tokenInfoRepository.flush();

        TokenInfo tokenInfo = TokenInfo.builder()
                .userId(userId)
                .accessToken(jwtInformation.getAccessToken())
                .refreshToken(jwtInformation.getRefreshToken())
                .build();

        tokenInfoRepository.save(tokenInfo);
    }

    @Override
    public void invalidateJwtInformationByUserId(Long userId) {
        tokenInfoRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return tokenInfoRepository.findByRefreshToken(refreshToken).isPresent();
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        tokenInfoRepository.findByRefreshToken(refreshToken)
                .ifPresent(tokenInfo -> {
                    tokenInfo.rotate(
                            newJwtInformation.getAccessToken(),
                            newJwtInformation.getRefreshToken()
                    );
                });
    }

    // 5분마다 만료된 Refresh Token 정리
    @Override
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpiredJwtInformation() {
        log.info("Clearing expired JWT Tokens");
        tokenInfoRepository.findAll().forEach(tokenInfo -> {
            // DB에 저장된 Refresh Token이 만료되었다면 삭제
            if (!jwtTokenProvider.validateToken(tokenInfo.getRefreshToken())) {
                tokenInfoRepository.delete(tokenInfo);
            }
        });
    }
}