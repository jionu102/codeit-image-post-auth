package codeit.sb06.imagepost.repository;

import codeit.sb06.imagepost.entity.TokenInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenInfoRepository extends JpaRepository<TokenInfo, Long> {
    Optional<TokenInfo> findByUserId(Long userId);
    Optional<TokenInfo> findByAccessToken(String accessToken);
    Optional<TokenInfo> findByRefreshToken(String refreshToken);
    void deleteByUserId(Long userId);
}