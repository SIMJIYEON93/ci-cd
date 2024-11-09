package com.example.cicd.oauth2;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");

            return OAuthAttributes.builder()
                    .name((String) response.get("nickname"))
                    .email((String) response.get("email"))
                    .attributes(response)
                    .nameAttributeKey("id")
                    .build();
        }
        throw new IllegalArgumentException("Unsupported registration ID");
    }
}
