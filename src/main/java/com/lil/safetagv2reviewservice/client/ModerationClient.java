package com.lil.safetagv2reviewservice.client;

import com.lil.safetagv2reviewservice.domain.ReviewStatus;
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

    public ReviewStatus moderateComment(String text) {
        Map<String, String> request = Map.of("text", text);

        try {
            // Appel POST au moderation-service
            Map response = restTemplate.postForObject(moderationUrl, request, Map.class);

            if (response != null && response.get("status") != null) {
                return ReviewStatus.valueOf(response.get("status").toString());
            }

            // Si la réponse est bizarre, on sécurise en le mettant en attente
            throw new IllegalStateException("Réponse invalide du service de modération.");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Si le moderation-service a renvoyé une erreur 4xx (texte refusé)
            throw new IllegalArgumentException("Le contenu de l'avis ne respecte pas nos règles de modération. Veuillez le modifier avant de publier.");

        } catch (Exception e) {
            throw new IllegalStateException("Le service de modération est temporairement indisponible. Veuillez réessayer plus tard.");
        }
    }
}
