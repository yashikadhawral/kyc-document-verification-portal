package com.kycportal.service;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Status;
import com.kycportal.domain.StatusHistory;
import com.kycportal.repository.KycRecordRepository;
import com.kycportal.repository.StatusHistoryRepository;
import com.kycportal.web.KycForm;
import java.io.IOException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Status changes and edits. Every status change is written to the history. */
@Service
public class WorkflowService {

    private final KycRecordRepository records;
    private final StatusHistoryRepository history;
    private final FileStorageService storage;

    public WorkflowService(KycRecordRepository records,
                           StatusHistoryRepository history,
                           FileStorageService storage) {
        this.records = records;
        this.history = history;
        this.storage = storage;
    }

    @Transactional
    public void startReview(Long id, AppUser reviewer) {
        KycRecord record = find(id);
        require(record, Status.SUBMITTED, "Only submitted records can be reviewed");
        change(record, Status.UNDER_REVIEW, reviewer.getFullName(), reviewer.getUsername(), "Review started");
    }

    @Transactional
    public void verify(Long id, AppUser reviewer) {
        KycRecord record = find(id);
        require(record, Status.UNDER_REVIEW, "Only records under review can be verified");
        record.setRejectionReason(null);
        change(record, Status.VERIFIED, reviewer.getFullName(), reviewer.getUsername(), "Verified");
    }

    @Transactional
    public void reject(Long id, AppUser reviewer, String reason) {
        KycRecord record = find(id);
        require(record, Status.UNDER_REVIEW, "Only records under review can be rejected");
        record.setRejectionReason(reason.trim());
        change(record, Status.REJECTED, reviewer.getFullName(), reviewer.getUsername(), reason.trim());
    }

    /** Saves edits. A rejected record goes back to Submitted. */
    @Transactional
    public void update(Long id, AppUser applicant, KycForm form, MultipartFile newDocument) throws IOException {
        KycRecord record = find(id);
        if (!record.getApplicant().getId().equals(applicant.getId())) {
            throw new AccessDeniedException("Applicants can only edit their own records");
        }
        if (record.getStatus() != Status.SUBMITTED && record.getStatus() != Status.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This record can no longer be edited");
        }

        record.setFullName(form.getFullName().trim());
        record.setDateOfBirth(form.getDateOfBirth());
        record.setIdType(form.getIdType());
        record.setIdNumber(form.getIdNumber().trim().toUpperCase(Locale.ROOT));

        if (newDocument != null && !newDocument.isEmpty()) {
            String original = StringUtils.cleanPath(
                newDocument.getOriginalFilename() == null ? "document" : newDocument.getOriginalFilename());
            if (original.length() > 200) {
                original = original.substring(original.length() - 200);
            }
            record.setDocumentPath(storage.store(newDocument));
            record.setDocumentName(original);
        }

        if (record.getStatus() == Status.REJECTED) {
            record.setRejectionReason(null);
            record.setReviewedBy(null);
            change(record, Status.SUBMITTED, null, applicant.getUsername(), "Resubmitted by applicant");
        } else {
            records.save(record);
        }
    }

    private KycRecord find(Long id) {
        return records.findWithApplicantById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static void require(KycRecord record, Status expected, String message) {
        if (record.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    private void change(KycRecord record, Status to, String reviewedBy, String changedBy, String note) {
        Status from = record.getStatus();
        record.setStatus(to);
        if (reviewedBy != null) {
            record.setReviewedBy(reviewedBy);
        }
        records.save(record);
        history.save(new StatusHistory(record, from, to, changedBy, note));
    }
}