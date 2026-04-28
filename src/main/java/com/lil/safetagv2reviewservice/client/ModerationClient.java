package com.lil.safetagreviewservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ModerationClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String moderationUrl;

    public ModerationClient(@Value("${services.moderationService}")  String moderationService) {
        this.moderationUrl = moderationService + "/check";
    }

    public boolean isReviewValid(String text) {
        Map<String, String> request = Map.of("text", text);

        try {
            // Appel POST au moderation-service
            Map response = restTemplate.postForObject(moderationUrl, request, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("isValid"));
        } catch (Exception e) {
            // En cas d'erreur (service down), on peut décider de bloquer ou d'accepter.
            // Choix pragmatique : on bloque par sécurité.
            return false;
        }
    }
}
