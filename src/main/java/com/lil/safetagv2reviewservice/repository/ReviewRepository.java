package com.lil.safetagv2reviewservice.repository;

import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Spring génère automatiquement la requête SQL pour trouver les avis d'un praticien
    List<Review> findByRppsId(String rppsId);
    Page<Review> findByRppsId(String rppsId, Pageable pageable);
    boolean existsByUserIdAndRppsId(UUID userId, String rppsId);
    List<Review> findByUserId(UUID userId);
    Page<Review> findByRppsIdAndStatus(String rppsId, ReviewStatus status, Pageable pageable);

    Optional<Review> findById(UUID reviewId);

    // On pourra en rajouter d'autres plus tard si besoin (ex: findByUserId)
}
