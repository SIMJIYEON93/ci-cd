package com.example.cicd.entity;

import com.example.cicd.dto.SignupRequestDto;
import com.example.cicd.oauth2.OAuthAttributes;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;


    @Column
    private Boolean needSocialSignup = true;


    public User(SignupRequestDto requestDto, String encodedPassword) {
        this.nickname=requestDto.getNickname();
        this.email = requestDto.getEmail();
        this.password = encodedPassword;
        this.role = UserRoleEnum.USER;
    }

    public User(OAuthAttributes attributes) {
        this.email = attributes.getEmail();
        this.password = UUID.randomUUID().toString();
        this.nickname = attributes.getName();
        this.needSocialSignup = true;
        this.role = UserRoleEnum.USER;
    }

    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    //test

}