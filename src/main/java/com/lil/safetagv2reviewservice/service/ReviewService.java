package com.lil.safetagv2reviewservice.service;

import com.lil.safetagv2reviewservice.client.ModerationClient;
import com.lil.safetagv2reviewservice.client.RppsClient;
import com.lil.safetagv2reviewservice.client.UserClient;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import com.lil.safetagv2reviewservice.exception.ResourceNotFoundException;
import com.lil.safetagv2reviewservice.models.UpdateReviewRequest;
import com.lil.safetagv2reviewservice.repository.ReviewRepository;
import com.lil.safetagv2reviewservice.repository.ReviewTagRepository;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ModerationClient moderationClient;
    private final UserClient userClient;
    private final RppsClient rppsClient;

    // L'injection de dépendance se fait via le constructeur
    public ReviewService(ReviewRepository reviewRepository, ReviewTagRepository reviewTagRepository, ModerationClient moderationClient, UserClient userClient, RppsClient rppsClient) {
        this.reviewRepository = reviewRepository;
        this.reviewTagRepository = reviewTagRepository;
        this.moderationClient = moderationClient;
        this.userClient = userClient;
        this.rppsClient = rppsClient;
    }

    @Transactional
    public Review createReview(Review review) {
        try {
            rppsClient.getPractitionerByRpps(review.getRppsId());
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Impossible de créer l'avis : le praticien RPPS " + review.getRppsId() + " est introuvable.");
        }
        if (!userClient.userExists(review.getUserId())) {
            throw new ResourceNotFoundException("Utilisateur introuvable pour l'ID : " + review.getUserId());
        }
        if (reviewRepository.existsByUserIdAndRppsId(review.getUserId(), review.getRppsId())) {
            throw new IllegalStateException("L'utilisateur a déjà publié un avis pour ce praticien. Vous pouvez modifier votre avis dans votre profil.");
        }

        if (review.getTags() != null) {
            for (ReviewTag tag : review.getTags()) {
                tag.setReview(review); // Indispensable pour que la clé étrangère soit remplie
            }
        }

        if (review.getComment() != null && !review.getComment().isBlank()) {
            // On délègue la décision du statut au moderation-service
            ReviewStatus status = moderationClient.moderateComment(review.getComment());
            review.setStatus(status);
        } else {
            // Un avis sans texte (juste une note) est approuvé par défaut
            review.setStatus(ReviewStatus.APPROVED);
        }

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByPractitioner(String rppsId) {
        return reviewRepository.findByRppsId(rppsId);
    }

    public Map<TagCategory, Double> getPractitionerStats(String rppsId) {
        List<ReviewTag> tags = reviewTagRepository.findByReview_RppsId(rppsId);

        // 1. On groupe les tags par catégorie
        Map<TagCategory, List<ReviewTag>> tagsByCategory = tags.stream()
                .collect(Collectors.groupingBy(ReviewTag::getCategory));

        Map<TagCategory, Double> stats = new HashMap<>();

        tagsByCategory.forEach((category, tagList) -> {
            // 2. On compte le nombre de votes positifs
            long positiveVotes = tagList.stream()
                    .filter(t -> t.getVote() == TagVote.POSITIVE)
                    .count();

            // 3. On calcule le pourcentage (Positifs / Total * 100)
            double percentage = (double) positiveVotes / tagList.size() * 100;

            // On arrondit à 1 décale pour la lisibilité
            stats.put(category, Math.round(percentage * 10.0) / 10.0);
        });

        return stats;
    }

    public Page<Review> getReviewsByRppsId(String rppsId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return reviewRepository.findByRppsIdAndStatus(rppsId, ReviewStatus.APPROVED, pageable);
    }

    public Review getReviewById(UUID id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable pour l'ID : " + id));
    }

    public List<Review> getReviewsByUser(UUID userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Review updateReview(UUID id, UUID userId, UpdateReviewRequest request) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!userClient.userExists(review.getUserId())) {
            throw new ResourceNotFoundException("Utilisateur introuvable pour l'ID : " + review.getUserId());
        }
        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized");
        }

        review.setComment(request.getComment());
        review.getTags().clear();
        if (request.getTags() != null) {
            for (ReviewTag tag : request.getTags()) {
                review.addTag(tag);
            }
        }
        review.getPathologies().clear();
        if (request.getPathologies() != null) {
                review.getPathologies().addAll(request.getPathologies());
        }
        review.setAddressIds(
                request.getAddressIds() != null ? new ArrayList<>(request.getAddressIds()) : new ArrayList<>()
        );
        if (review.getComment() != null && !review.getComment().isBlank()) {
            ReviewStatus status = moderationClient.moderateComment(review.getComment());
            review.setStatus(status);
        } else {
            review.setStatus(ReviewStatus.APPROVED);
        }

        return reviewRepository.save(review);

    }

    public void reportReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis introuvable"));

        // On bascule le statut pour le masquer immédiatement
        review.setStatus(ReviewStatus.REPORTED);
        reviewRepository.save(review);
    }

    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
        review.setStatus(ReviewStatus.REJECTED); // Ou utilise un Enum si tu en as un
        review.setRejectionReason(reason);
        reviewRepository.save(review);
    }
}
