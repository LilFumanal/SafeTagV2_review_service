package com.lil.safetagreviewservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Component
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String userUrl;

    public UserClient(@Value("${services.userService}")String userService) {
        this.userUrl = userService;
    }

    public boolean userExists(UUID userId) {
        try {
            restTemplate.getForObject(
                    userUrl + "/" + userId,
                    Object.class
            );
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
