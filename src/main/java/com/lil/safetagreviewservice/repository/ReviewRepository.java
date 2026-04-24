package com.lil.safetagreviewservice.repository;

import com.lil.safetagreviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Spring génère automatiquement la requête SQL pour trouver les avis d'un praticien
    List<Review> findByRppsId(String rppsId);
    Page<Review> findByRppsId(String rppsId, Pageable pageable);

    // On pourra en rajouter d'autres plus tard si besoin (ex: findByUserId)
}
