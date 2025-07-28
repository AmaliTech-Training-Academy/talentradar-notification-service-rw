package com.talentradar.talentradarnotificationservicerw.config;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.UserClaimsDTO;
import com.talentradar.talentradarnotificationservicerw.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CookieAuthHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
if (request instanceof HttpServletRequest servletRequest) {

            UserClaimsDTO userClaims = jwtUtil.extractClaimsFromHeader(servletRequest);

            UserPrincipal userPrincipal = new UserPrincipal(
                    userClaims.userId(),
                    userClaims.email(),
                    userClaims.fullName(),
                    userClaims.role()
            );

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userClaims.role()));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            servletRequest.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            attributes.put("user", authentication);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
