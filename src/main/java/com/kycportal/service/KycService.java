package com.kycportal.service;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Status;
import com.kycportal.domain.StatusHistory;
import com.kycportal.repository.KycRecordRepository;
import com.kycportal.repository.StatusHistoryRepository;
import com.kycportal.web.KycForm;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KycService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();
    private final KycRecordRepository records;
    private final StatusHistoryRepository history;
    private final FileStorageService storage;

    public KycService(KycRecordRepository records,
                      StatusHistoryRepository history,
                      FileStorageService storage) {
        this.records = records;
        this.history = history;
        this.storage = storage;
    }

    @Transactional
    public KycRecord create(AppUser applicant, KycForm form, MultipartFile document) throws IOException {
        String storedName = storage.store(document);
        String original = StringUtils.cleanPath(
            document.getOriginalFilename() == null ? "document" : document.getOriginalFilename());
        if (original.length() > 200) {
            original = original.substring(original.length() - 200);
        }

        KycRecord record = new KycRecord();
        record.setApplicant(applicant);
        record.setReferenceNo(newReferenceNo());
        record.setFullName(form.getFullName().trim());
        record.setDateOfBirth(form.getDateOfBirth());
        record.setIdType(form.getIdType());
        record.setIdNumber(form.getIdNumber().trim().toUpperCase(Locale.ROOT));
        record.setDocumentName(original);
        record.setDocumentPath(storedName);
        record.setStatus(Status.SUBMITTED);
        records.save(record);

        history.save(new StatusHistory(record, null, Status.SUBMITTED,
            applicant.getUsername(), "Submitted by applicant"));
        return record;
    }

    private String newReferenceNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String reference;
        do {
            reference = "KYC-" + date + "-" + randomCode(4);
        } while (records.existsByReferenceNo(reference));
        return reference;
    }

    private String randomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
}