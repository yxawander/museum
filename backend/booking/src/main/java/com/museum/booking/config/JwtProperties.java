package com.museum.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥，长度至少 32 字节 */
    private String secret = "HubeiMuseumBookingSystemSecretKey2026ForJwtTokenSign!";
    /** 过期时间（小时） */
    private long expireHours = 24;
}