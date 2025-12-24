package codeit.sb06.imagepost.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 동시 로그인 제한(1명)을 위한 unique 제약
    private Long userId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    public void rotate(String newAccess, String newRefresh) {
        this.accessToken = newAccess;
        this.refreshToken = newRefresh;
    }
}