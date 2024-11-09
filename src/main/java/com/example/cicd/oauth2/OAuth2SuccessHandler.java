package com.example.cicd.oauth2;

import com.example.cicd.entity.User;
import com.example.cicd.security.TokenResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenResponseHandler tokenResponseHandler;

    @Value("${oauth2.redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        User user = (User) attributes.get("storedUser");

        tokenResponseHandler.addTokensToResponseForSocialLogin(user, response);

        String redirectUrlWithToken = redirectUrl + "?userId=" + user.getId();

        response.sendRedirect(redirectUrlWithToken);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"OAuth2 로그인 성공\",\"userId\":\"" + user.getId() + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}