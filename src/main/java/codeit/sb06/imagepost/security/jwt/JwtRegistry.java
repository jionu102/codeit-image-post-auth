package codeit.sb06.imagepost.security.jwt;

public interface JwtRegistry {
    void registerJwtInformation(JwtInformation jwtInformation);
    void invalidateJwtInformationByUserId(Long userId);
    boolean hasActiveJwtInformationByRefreshToken(String refreshToken);
    void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);
    void clearExpiredJwtInformation();
}