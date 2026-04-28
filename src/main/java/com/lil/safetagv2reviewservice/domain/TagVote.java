package com.lil.safetagv2reviewservice.domain;

public enum TagVote {
    POSITIVE(1),
    NEGATIVE(-1),
    NEUTRAL(0);

    private final int value;

    TagVote(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
