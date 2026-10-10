package com.kycportal.repository;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycRecordRepository extends JpaRepository<KycRecord, Long> {

    List<KycRecord> findByApplicantOrderByCreatedAtDesc(AppUser applicant);

    long countByStatus(Status status);

    long countByApplicantAndStatus(AppUser applicant, Status status);
}
