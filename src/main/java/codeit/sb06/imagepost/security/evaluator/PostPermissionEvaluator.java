package codeit.sb06.imagepost.security.evaluator;

import codeit.sb06.imagepost.entity.Post;
import codeit.sb06.imagepost.exception.PostNotFoundException;
import codeit.sb06.imagepost.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("Post")
@RequiredArgsConstructor
public class PostPermissionEvaluator implements DomainPermissionEvaluator{
    private final PostRepository postRepository;


    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String permission) {
        Post post = postRepository.findById((Long) targetId)
                .orElseThrow(PostNotFoundException::new);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        return post.getAuthor().getUsername().equals(authentication.getName());
    }
}
