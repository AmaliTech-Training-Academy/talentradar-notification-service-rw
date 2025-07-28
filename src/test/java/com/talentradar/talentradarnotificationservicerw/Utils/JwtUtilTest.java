package com.talentradar.talentradarnotificationservicerw.Utils;

import com.talentradar.talentradarnotificationservicerw.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "dGVzdC1zZWNyZXQtdGhhdC1pcy1sb25nLWVub3VnaC1mb3ItaG1hYy1zaGEyNTY="; // Base64 encoded test secret
    private final int testExpirationMs = 3600000; // 1 hour
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(testSecret, testExpirationMs);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
    }

    private String createTestToken(String email, String role, Date expiration) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    private String createTestToken(String email, String role) {
        return createTestToken(email, role, new Date(System.currentTimeMillis() + testExpirationMs));
    }

    @Test
    void getEmailFromToken_ValidToken_ReturnsEmail() {
        // Arrange
        String expectedEmail = "test@example.com";
        String token = createTestToken(expectedEmail, "USER");

        // Act
        String actualEmail = jwtUtil.getEmailFromToken(token);

        // Assert
        assertEquals(expectedEmail, actualEmail);
    }

    @Test
    void getEmailFromToken_TokenWithNullSubject_ReturnsNull() {
        // Arrange
        String token = Jwts.builder()
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
                .signWith(signingKey)
                .compact();

        // Act
        String email = jwtUtil.getEmailFromToken(token);

        // Assert
        assertNull(email);
    }

    @Test
    void getRoleFromToken_ValidToken_ReturnsRole() {
        // Arrange
        String expectedRole = "ADMIN";
        String token = createTestToken("user@example.com", expectedRole);

        // Act
        String actualRole = jwtUtil.getRoleFromToken(token);

        // Assert
        assertEquals(expectedRole, actualRole);
    }

    @Test
    void getRoleFromToken_TokenWithoutRole_ReturnsNull() {
        // Arrange
        String token = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
                .signWith(signingKey)
                .compact();

        // Act
        String role = jwtUtil.getRoleFromToken(token);

        // Assert
        assertNull(role);
    }
    @Test
    void getClaimFromToken_ValidToken_ReturnsClaimValue() {
        // Arrange
        String email = "test@example.com";
        String token = createTestToken(email, "USER");

        // Act
        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);

        // Assert
        assertEquals(email, subject);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()));
    }

    @Test
    void getClaimFromToken_CustomClaim_ReturnsCustomClaimValue() {
        // Arrange
        String customValue = "customValue";
        String token = Jwts.builder()
                .subject("user@example.com")
                .claim("customClaim", customValue)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
                .signWith(signingKey)
                .compact();

        // Act
        String result = jwtUtil.getClaimFromToken(token, claims -> claims.get("customClaim", String.class));

        // Assert
        assertEquals(customValue, result);
    }

    @Test
    void getEmailFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getEmailFromToken(invalidToken)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void getRoleFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getRoleFromToken(invalidToken)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getClaimFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getClaimFromToken(invalidToken, Claims::getSubject)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getEmailFromToken_ExpiredToken_ThrowsException() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        String expiredToken = createTestToken("user@example.com", "USER", pastDate);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getEmailFromToken(expiredToken)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
        assertTrue(exception.getCause() instanceof JwtException);
    }

    @Test
    void getRoleFromToken_ExpiredToken_ThrowsException() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        String expiredToken = createTestToken("user@example.com", "USER", pastDate);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getRoleFromToken(expiredToken)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getEmailFromToken_TokenSignedWithDifferentKey_ThrowsException() {
        // Arrange
        SecretKey differentKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("ZGlmZmVyZW50LXNlY3JldC10aGF0LWlzLWxvbmctZW5vdWdoLWZvci1obWFjLXNoYTI1Ng=="));
        String tokenWithDifferentKey = Jwts.builder()
                .subject("user@example.com")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
                .signWith(differentKey)
                .compact();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getEmailFromToken(tokenWithDifferentKey)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getEmailFromToken_NullToken_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getEmailFromToken(null)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getRoleFromToken_EmptyToken_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getRoleFromToken("")
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getClaimFromToken_MalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "header.payload"; // Missing signature

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getClaimFromToken(malformedToken, Claims::getSubject)
        );
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void multipleClaimsFromSameToken_ValidToken_AllClaimsReturned() {
        // Arrange
        String email = "multi@example.com";
        String role = "MANAGER";
        String token = createTestToken(email, role);

        // Act
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        String extractedRole = jwtUtil.getRoleFromToken(token);
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);
        Date expiration = jwtUtil.getClaimFromToken(token, Claims::getExpiration);

        // Assert
        assertEquals(email, extractedEmail);
        assertEquals(role, extractedRole);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(issuedAt.before(expiration));
    }
}