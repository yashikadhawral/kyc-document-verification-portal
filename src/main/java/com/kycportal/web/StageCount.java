package com.kycportal.web;

import com.kycportal.domain.Status;

/** One stage of the workflow with its record count, used by the overview page. */
public class StageCount {

    private final Status status;
    private final long count;

    public StageCount(Status status, long count) {
        this.status = status;
        this.count = count;
    }

    public String getKey() { return status.getCssKey(); }
    public String getLabel() { return status.getLabel(); }
    public long getCount() { return count; }
}
