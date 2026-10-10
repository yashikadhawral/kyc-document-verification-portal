package com.kycportal.repository;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KycRecordRepository extends JpaRepository<KycRecord, Long> {

    List<KycRecord> findByApplicantOrderByCreatedAtDesc(AppUser applicant);

    long countByStatus(Status status);

    long countByApplicantAndStatus(AppUser applicant, Status status);

    boolean existsByReferenceNo(String referenceNo);

    @Query("select r from KycRecord r join fetch r.applicant where r.id = :id")
    Optional<KycRecord> findWithApplicantById(@Param("id") Long id);
}