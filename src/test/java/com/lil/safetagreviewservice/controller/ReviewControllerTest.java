package com.lil.safetagreviewservice.controller; // Vérifie que c'est le bon package

import com.lil.safetagreviewservice.entity.Review;
import com.lil.safetagreviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void createReview_ShouldReturn201Created() throws Exception {
        // Arrange
        Review mockReview = new Review();
        mockReview.setId(1L);

        when(reviewService.createReview(any(Review.class))).thenReturn(mockReview);

        // Act & Assert
        // On simule une requête POST avec un JSON basique
        String jsonPayload = """
                {
                    "rppsId": "12345678910",
                    "userId": "user-1",
                    "addressIds": ["cabinet1"],
                    "comment": "Très bon praticien"
                }
                """;

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated()); // Vérifie qu'on obtient bien HTTP 201
    }
    @Test
    void getReviewsByRppsId_ShouldReturn200Ok() throws Exception {
        // Arrange
        String rppsId = "12345678910";
        Review review = new Review();
        review.setId(1L);
        review.setRppsId(rppsId);
        review.setAddressIds(java.util.List.of("cabinet1"));
        review.setComment("Très bon praticien");

        org.springframework.data.domain.Page<Review> reviewPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(review));

        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenReturn(reviewPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].comment").value("Très bon praticien"));
    }
    @Test
    void getReviewsByRppsId_ShouldReturn500WhenServiceFails() throws Exception {
        // Arrange
        String rppsId = "12345678910";
        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Erreur base de données"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createReview_ShouldReturn400_WhenNoConsultationMode() throws Exception {
        // JSON sans adresse et sans téléconsultation (supposé isTeleconsultation: false par défaut)
        String invalidJson = """
        {
          "rppsId": "123456789",
          "userId": "user_1",
          "comment": "Un commentaire assez long pour passer la validation.",
          "addressIds": [],
          "isTeleconsultation": false,
          "tags": [],
          "pathologies": []
        }
        """;

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                // On vérifie que le message d'erreur spécifique est présent
                .andExpect(jsonPath("$.details.consultationModeValid").value("Veuillez renseigner au moins un mode de consultation (visio ou adresse physique)"));
    }

    @Test
    void createReview_ShouldReturn400_WhenInvalidPathology() throws Exception {
        String invalidEnumJson = """
        {
          "rppsId": "123456789",
          "userId": "user_1",
          "comment": "Un commentaire valide.",
          "isTeleconsultation": true,
          "pathologies": ["PATHOLOGIE_IMPOSSIBLE"]
        }
        """;

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEnumJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        String rppsId = "12345678910";

        // Simulation d'une panne de service
        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failure"));

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn400_WhenRppsIdIsInvalid() throws Exception {
        // Un RPPS invalide (contient des lettres au lieu de 11 chiffres)
        String invalidRppsId = "12345ABCDEF";

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", invalidRppsId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
