package com.example.cicd.security;

import com.example.cicd.entity.RefreshToken;
import com.example.cicd.entity.User;
import com.example.cicd.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenResponseHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public void addTokensToResponse(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken();

        addAccessTokenToHeader(accessToken, response);
        addRefreshTokenToCookie(refreshToken, response);

        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getId()));
    }


    public void addTokensToResponseForSocialLogin(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken();

        addAccessTokenToCookie(accessToken, response);
        addRefreshTokenToCookie(refreshToken, response);

        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getId()));
    }

    private void addAccessTokenToHeader(String accessToken, HttpServletResponse response) {
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + accessToken);
    }

    private void addAccessTokenToCookie(String accessToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) JwtUtil.ACCESS_TOKEN_TIME / 1000);
        response.addCookie(cookie);
    }

    private void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) JwtUtil.REFRESH_TOKEN_TIME / 1000);
        response.addCookie(cookie);
    }
}
