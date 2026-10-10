package com.kycportal.domain;

public enum Role {
    APPLICANT("Applicant"),
    VERIFIER("Verifier"),
    ADMIN("Admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
