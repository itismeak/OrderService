package com.microservice.order_service.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("❌ JWT Token Expired: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("❌ Invalid JWT Signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Invalid JWT Token: {}", e.getMessage());
            throw e;
        }
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            log.info("✅ JWT Token is valid");
            return true;
        } catch (Exception e) {
            log.error("❌ JWT Validation Failed: {}", e.getMessage());
            return false;
        }
    }
}