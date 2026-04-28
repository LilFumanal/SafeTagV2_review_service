package com.lil.safetagv2reviewservice.repository;

import com.lil.safetagv2reviewservice.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {
    // On récupère tous les tags dont l'avis lié possède cet rppsId
    List<ReviewTag> findByReview_RppsId(String rppsId);
}
