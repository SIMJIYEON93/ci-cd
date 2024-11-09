package com.example.cicd.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // user
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "U001", "존재하지 않는 사용자 입니다."),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "U002", "이미 가입된 이메일입니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "U005", "비밀번호가 일치하지 않습니다."),

    //auth
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AU001", "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AU002", "유효하지 않은 토큰입니다."),
    BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "AU003", "블랙리스트에 등록된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU004", "존재하지 않는 리프레시 토큰입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
