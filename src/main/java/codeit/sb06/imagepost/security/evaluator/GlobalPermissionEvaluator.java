package codeit.sb06.imagepost.security.evaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalPermissionEvaluator implements PermissionEvaluator {
    private final Map<String, DomainPermissionEvaluator> permissionEvaluators;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication,  Serializable targetId, String targetType, Object permission) {
        log.info("Permission check for targetType: {}", targetType);
        DomainPermissionEvaluator permissionEvaluator = permissionEvaluators.get(targetType);

        if (permissionEvaluator == null) {
            throw new IllegalArgumentException("Permission evaluator not found for targetType: " + targetType);
        }
        return permissionEvaluator.hasPermission(authentication, targetId, (String) permission);
    }
}
