package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/reviews")
public class ModerationController {

    private final ReviewService reviewService;

    public ModerationController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable Long id,
            @RequestBody RejectionPayload payload) {

        reviewService.rejectReview(id, payload.rejectionReason());

        return ResponseEntity.ok().build();
    }
}

record RejectionPayload(String rejectionReason) {}
