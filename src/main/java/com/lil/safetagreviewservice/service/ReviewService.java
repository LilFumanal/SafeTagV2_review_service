package com.lil.safetagreviewservice.service;

import com.lil.safetagreviewservice.domain.TagCategory;
import com.lil.safetagreviewservice.domain.TagVote;
import com.lil.safetagreviewservice.entity.Review;
import com.lil.safetagreviewservice.entity.ReviewTag;
import com.lil.safetagreviewservice.exception.ResourceNotFoundException;
import com.lil.safetagreviewservice.repository.ReviewRepository;
import com.lil.safetagreviewservice.repository.ReviewTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;

    // L'injection de dépendance se fait via le constructeur
    public ReviewService(ReviewRepository reviewRepository, ReviewTagRepository reviewTagRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewTagRepository = reviewTagRepository;
    }

    @Transactional
    public Review createReview(Review review) {
        // Plus tard, on pourra ajouter ici la logique métier :
        // - Vérifier que l'utilisateur n'a pas déjà noté ce praticien
        // - Appeler le rpps-service pour valider le rppsId (si nécessaire)
        // Lier chaque tag à l'avis pour satisfaire la relation bidirectionnelle
        if (review.getTags() != null) {
            for (ReviewTag tag : review.getTags()) {
                tag.setReview(review); // Indispensable pour que la clé étrangère soit remplie
            }
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

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable pour l'ID : " + id));
    }

}
