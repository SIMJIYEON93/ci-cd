package com.example.cicd.service;

import com.example.cicd.dto.LogoutRequestDto;
import com.example.cicd.dto.ReissueRequestDto;
import com.example.cicd.dto.SignupRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface UserService {
    @Transactional
    void signup(SignupRequestDto requestDto);

    void reissueToken(ReissueRequestDto requestDto, HttpServletResponse response);

    void logout(LogoutRequestDto requestDto, HttpServletRequest request);

}
