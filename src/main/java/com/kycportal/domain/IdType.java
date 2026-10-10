package com.kycportal.domain;

public enum IdType {
    PAN("PAN card"),
    PASSPORT("Passport"),
    VOTER_ID("Voter ID"),
    DRIVING_LICENCE("Driving licence"),
    AADHAAR("Aadhaar");

    private final String label;

    IdType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
