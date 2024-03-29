package com.shopapp.security;

import com.shopapp.user.Role;
import com.shopapp.user.User;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class UserAccessDecisionManager implements AuthorizationManager<RequestAuthorizationContext> {

    // Checks if principal (CUSTOMER) is trying to access his own data by comparing request path variable user id with principal id
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext ctx) {
        try {
            User principal = (User) authenticationSupplier.get().getPrincipal();

            if (principal.getRole() == Role.ROLE_ADMIN || principal.getRole() == Role.ROLE_EMPLOYEE)
                return new AuthorizationDecision(true);

            Long userId = Long.parseLong(ctx.getVariables().get("id"));

            return new AuthorizationDecision(userId.equals(principal.getUserId()));
        } catch (RuntimeException e) {
            return  new AuthorizationDecision(false);
        }
    }
}
