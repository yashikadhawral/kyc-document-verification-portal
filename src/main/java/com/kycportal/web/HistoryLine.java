package com.kycportal.web;

/** One line of the status history, already formatted for display. */
public class HistoryLine {

    private final String when;
    private final String change;
    private final String by;
    private final String note;

    public HistoryLine(String when, String change, String by, String note) {
        this.when = when;
        this.change = change;
        this.by = by;
        this.note = note;
    }

    public String getWhen() { return when; }
    public String getChange() { return change; }
    public String getBy() { return by; }
    public String getNote() { return note; }
}