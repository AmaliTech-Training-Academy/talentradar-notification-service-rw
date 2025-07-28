package com.talentradar.talentradarnotificationservicerw.utils;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.UserClaimsDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final String secret;
    private final int jwtExpirationInSeconds;

    public JwtUtil(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_EXPIRATION_MS}") int jwtExpirationInSeconds
    ) {
        this.secret = secret;
        this.jwtExpirationInSeconds = jwtExpirationInSeconds;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getRoleFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public UserClaimsDTO extractClaimsFromHeader(HttpServletRequest request) {

        return UserClaimsDTO.builder()
                .userId(request.getHeader("X-User-Id"))
                .email(request.getHeader("X-User-Email"))
                .fullName(request.getHeader("X-User-FullName"))
                .role(request.getHeader("X-User-Role"))
                .build();
    }
}
