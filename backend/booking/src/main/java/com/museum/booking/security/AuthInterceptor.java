package com.museum.booking.security;

import com.museum.booking.common.BusinessException;
import com.museum.booking.entity.SysUser;
import com.museum.booking.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 登录与角色鉴权拦截器。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }

        Claims claims = jwtUtil.parse(authorization.substring(7));
        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录状态异常，请重新登录");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "账号不存在，请重新登录");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        UserContext.set(new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole()));

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin") && !"ADMIN".equals(user.getRole()) && !"STAFF".equals(user.getRole())) {
            throw new BusinessException(403, "无权限访问管理端");
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && Arrays.stream(requireRole.value()).noneMatch(r -> r.equals(user.getRole()))) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}