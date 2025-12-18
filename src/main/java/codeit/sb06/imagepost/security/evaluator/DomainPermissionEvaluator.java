package codeit.sb06.imagepost.security.evaluator;

import org.springframework.security.core.Authentication;

import java.io.Serializable;

public interface DomainPermissionEvaluator {
    boolean hasPermission(Authentication authentication, Serializable targetId, String permission);
}
