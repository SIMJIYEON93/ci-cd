package com.example.cicd.security;

import com.example.cicd.exception.ErrorCode;
import com.example.cicd.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtUtil.getClaims(token);
                setAuthentication(claims.getSubject());
            } catch (ExpiredJwtException e){
                handleTokenError(response, ErrorCode.EXPIRED_TOKEN);
                return;
            } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                handleTokenError(response, ErrorCode.INVALID_TOKEN);
                return;
            }
            if(jwtUtil.isTokenBlacklisted(token)) {
                handleTokenError(response, ErrorCode.BLACKLISTED_TOKEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleTokenError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponse responseDto = ErrorResponse.of(errorCode);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    public void setAuthentication(String username) throws UsernameNotFoundException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String email) throws UsernameNotFoundException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}