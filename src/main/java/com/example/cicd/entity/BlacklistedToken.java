package com.example.cicd.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "blacklist")
public class BlacklistedToken {

    @Id
    private String accessToken;

    @TimeToLive
    private Long expirationSeconds;

    public BlacklistedToken(String accessToken, Long expirationSeconds) {
        this.accessToken = accessToken;
        this.expirationSeconds = expirationSeconds;
    }
}
