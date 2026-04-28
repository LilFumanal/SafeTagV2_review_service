package com.lil.safetagv2reviewservice.entity;

import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "review_tags")
public class ReviewTag {

    @Id
    @GeneratedValue // Hibernate détectera automatiquement qu'il doit générer un UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagVote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    public ReviewTag() {}

    public ReviewTag(TagCategory category, TagVote vote, Review review) {
        this.category = category;
        this.vote = vote;
        this.review = review;
    }
}
