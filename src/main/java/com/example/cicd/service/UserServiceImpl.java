package com.example.cicd.service;

import com.example.cicd.dto.LogoutRequestDto;
import com.example.cicd.dto.ReissueRequestDto;
import com.example.cicd.dto.SignupRequestDto;
import com.example.cicd.entity.BlacklistedToken;
import com.example.cicd.entity.RefreshToken;
import com.example.cicd.entity.User;
import com.example.cicd.exception.CustomException;
import com.example.cicd.exception.ErrorCode;
import com.example.cicd.repository.BlacklistedTokenRepository;
import com.example.cicd.repository.RefreshTokenRepository;
import com.example.cicd.repository.UserRepository;
import com.example.cicd.security.JwtUtil;
import com.example.cicd.security.TokenResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenResponseHandler tokenResponseHandler;


        @Override
        @Transactional
        public void signup(SignupRequestDto requestDto) {

            verifyEmailIsUnique(requestDto.getEmail());

            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

            User user = new User(requestDto, encodedPassword);
            userRepository.save(user);
        }


    @Override
    public void reissueToken(ReissueRequestDto requestDto, HttpServletResponse response) {
        String refreshToken = requestDto.getRefreshToken();
        validateToken(refreshToken);

        RefreshToken refreshTokenEntity = findRefreshTokenEntity(refreshToken);
        User user = findUserById(refreshTokenEntity.getUserId());

        tokenResponseHandler.addTokensToResponse(user, response);
        refreshTokenRepository.delete(refreshTokenEntity);
    }


    @Override
    public void logout(LogoutRequestDto requestDto, HttpServletRequest request) {
        String refreshToken = requestDto.getRefreshToken();
        validateToken(refreshToken);

        RefreshToken refreshTokenEntity = findRefreshTokenEntity(refreshToken);
        refreshTokenRepository.delete(refreshTokenEntity);

        String accessToken = jwtUtil.extractTokenFromRequest(request);
        blacklistToken(accessToken);
    }


    private void validateToken(String token) {
        if (!jwtUtil.isValidToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private RefreshToken findRefreshTokenEntity(String refreshToken) {
        return refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    private void verifyEmailIsUnique(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void blacklistToken(String token) {
        long expirationTime = jwtUtil.getRemainingTime(token);
        blacklistedTokenRepository.save(new BlacklistedToken(token, expirationTime / 1000));
    }
}
