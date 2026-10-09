package com.librax.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librax.model.Role;
import com.librax.model.User;
import com.librax.repository.UserRepository;
import com.librax.web.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Component
@SuppressWarnings("null")
public class PermissionInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PermissionInterceptor(JwtTokenProvider tokenProvider, UserRepository userRepository, ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenProvider.validateToken(token)) {
                Claims claims = tokenProvider.getClaims(token);
                Integer userId = (Integer) claims.get("userId");
                String username = claims.getSubject();
                String roleStr = (String) claims.get("role");

                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                user.setRole(Role.valueOf(roleStr));
                user.setFullName((String) claims.get("fullName"));
                user.setEmail((String) claims.get("email"));

                @SuppressWarnings("unchecked")
                List<String> perms = (List<String>) claims.get("permissions");
                if (perms != null) {
                    user.setPermissions(new HashSet<>(perms));
                } else if (userId != null) {
                    user.setPermissions(userRepository.findPermissionsByUserId(userId));
                }

                SecurityContextHolder.setUser(user);
            }
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            RequiresPermission reqPerm = handlerMethod.getMethodAnnotation(RequiresPermission.class);
            if (reqPerm == null) {
                reqPerm = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
            }

            if (reqPerm != null) {
                User currentUser = SecurityContextHolder.getUser();
                if (currentUser == null) {
                    writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_REQUIRED", "Authentication required to access this resource.");
                    return false;
                }

                // SUPER_ADMIN role bypasses individual permission checks
                if (currentUser.getRole() == Role.SUPER_ADMIN) {
                    return true;
                }

                if (!currentUser.hasPermission(reqPerm.value())) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                            "Access denied: Missing required permission '" + reqPerm.value() + "'.");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        SecurityContextHolder.clear();
    }

    private void writeError(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> apiResp = ApiResponse.error(errorCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResp));
    }
}
