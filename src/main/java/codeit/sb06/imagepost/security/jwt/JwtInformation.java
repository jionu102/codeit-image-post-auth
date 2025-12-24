package codeit.sb06.imagepost.security.jwt;

import codeit.sb06.imagepost.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtInformation {
    private UserDto userDto;
    private String accessToken;
    private String refreshToken;
}