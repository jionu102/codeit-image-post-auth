package codeit.sb06.imagepost.dto;

import codeit.sb06.imagepost.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String role;

    // Entity -> DTO 변환 편의 메서드
    public static UserDto from(Member member) {
        return UserDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .role(member.getRole().name())
                .build();
    }
}