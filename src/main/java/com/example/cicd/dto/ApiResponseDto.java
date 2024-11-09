package com.example.cicd.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto {
    private Integer statusCode;
    private String message;

    public ApiResponseDto(String message, Integer statusCode) {
        this.statusCode = statusCode;
        this.message = message;
    }
}