package com.museum.booking.security;

import com.museum.booking.common.BusinessException;
import com.museum.booking.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil createJwtUtil() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("UnitTestSecretKeyForMuseumBookingSystem2026AtLeast32Bytes!");
        properties.setExpireHours(2);
        return new JwtUtil(properties);
    }

    @Test
    void shouldGenerateAndParseToken() {
        JwtUtil jwtUtil = createJwtUtil();
        String token = jwtUtil.generate(3L, "visitor", "VISITOR");

        Claims claims = jwtUtil.parse(token);

        assertThat(claims.getSubject()).isEqualTo("3");
        assertThat(claims.get("username", String.class)).isEqualTo("visitor");
        assertThat(claims.get("role", String.class)).isEqualTo("VISITOR");
    }

    @Test
    void shouldRejectInvalidToken() {
        JwtUtil jwtUtil = createJwtUtil();

        assertThatThrownBy(() -> jwtUtil.parse("not-a-jwt-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("登录状态");
    }
}