package com.lil.safetagreviewservice.models;

import com.lil.safetagreviewservice.domain.PathologyFamily;
import com.lil.safetagreviewservice.entity.ReviewTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateReviewRequest {

    private String comment;
    private List<ReviewTag> tags;
    private List<PathologyFamily> pathologies;
    private List<UUID> addressIds;
    private boolean isTeleconsultation;
}
