package com.lil.safetagreviewservice.entity;

import com.lil.safetagreviewservice.domain.TagCategory;
import com.lil.safetagreviewservice.domain.TagVote;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
