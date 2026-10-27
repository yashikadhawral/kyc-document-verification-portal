package com.kycportal.web;

/** One line of the admin audit trail, already formatted for display. */
public class ActivityLine {

    private final String when;
    private final Long recordId;
    private final String reference;
    private final String change;
    private final String by;
    private final String note;

    public ActivityLine(String when, Long recordId, String reference, String change, String by, String note) {
        this.when = when;
        this.recordId = recordId;
        this.reference = reference;
        this.change = change;
        this.by = by;
        this.note = note;
    }

    public String getWhen() { return when; }
    public Long getRecordId() { return recordId; }
    public String getReference() { return reference; }
    public String getChange() { return change; }
    public String getBy() { return by; }
    public String getNote() { return note; }
}