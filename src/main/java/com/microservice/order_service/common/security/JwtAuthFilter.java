package com.microservice.order_service.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.order_service.common.constants.AppConstant;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.CurrentUser;
import com.microservice.order_service.common.dto.ErrorDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String SECRET;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Public endpoints allow without token
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            handleException(response, "Missing or invalid Authorization header", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            Object roleObj = claims.get("role");

            Long userId = claims.get("userId", Long.class);
            String name = claims.get("name", String.class);
            String role = claims.get("role", String.class);

            //List<SimpleGrantedAuthority> authorities = extractAuthorities(roleObj);
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));
            // Build CurrentUser object
            CurrentUser currentUser = new CurrentUser(
                    userId,
                    email,
                    name,
                    role
            );
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            handleException(response, "Invalid or expired token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return Arrays.stream(AppConstant.PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Object roleObj) {
        if (roleObj instanceof String) {
            return List.of(new SimpleGrantedAuthority((String) roleObj));
        } else if (roleObj instanceof Collection<?>) {
            Collection<?> roles = (Collection<?>) roleObj;
            List<SimpleGrantedAuthority> list = new ArrayList<>();
            for (Object r : roles) {
                list.add(new SimpleGrantedAuthority(r.toString()));
            }
            return list;
        }
        return List.of();
    }

    private void handleException(HttpServletResponse response,
                                 String message,
                                 int status) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        ErrorDetail payload = new ErrorDetail(
                Instant.now().toString(),
                status,
                message
        );

        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(
                "Authentication failed",
                payload,
                false
        );

        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}