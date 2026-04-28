package com.lil.safetagreviewservice.service;

import com.lil.safetagreviewservice.client.ModerationClient;
import com.lil.safetagreviewservice.client.RppsClient;
import com.lil.safetagreviewservice.client.UserClient;
import com.lil.safetagreviewservice.domain.TagCategory;
import com.lil.safetagreviewservice.domain.TagVote;
import com.lil.safetagreviewservice.entity.Review;
import com.lil.safetagreviewservice.entity.ReviewTag;
import com.lil.safetagreviewservice.exception.ResourceNotFoundException;
import com.lil.safetagreviewservice.models.UpdateReviewRequest;
import com.lil.safetagreviewservice.repository.ReviewRepository;
import com.lil.safetagreviewservice.repository.ReviewTagRepository;
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
        boolean isValid = moderationClient.isReviewValid(review.getComment());

        if (!isValid) {
            throw new IllegalArgumentException("Le contenu de l'avis ne respecte pas nos règles de modération.");
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
        // Tri par défaut : du plus récent au plus ancien
        // (Modifie "createdAt" par le nom exact de ton champ de date si différent)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return reviewRepository.findByRppsId(rppsId, pageable);
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

        return reviewRepository.save(review);
    }

}
