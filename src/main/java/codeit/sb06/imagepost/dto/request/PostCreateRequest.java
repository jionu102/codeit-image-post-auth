package codeit.sb06.imagepost.dto.request;

import codeit.sb06.imagepost.dto.request.validator.ValidContent;
import codeit.sb06.imagepost.dto.request.validator.ValidTitle;
import codeit.sb06.imagepost.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record PostCreateRequest(
        @ValidTitle
        String title,

        @ValidContent
        String content,

        List<@NotBlank(message = "태그는 공백일 수 없습니다.") String> tags
) {

}