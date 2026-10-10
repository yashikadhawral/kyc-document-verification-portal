package com.kycportal.domain;

public enum Status {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under review"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** CSS-friendly key, for example "under-review". */
    public String getCssKey() {
        return name().toLowerCase().replace('_', '-');
    }
}
