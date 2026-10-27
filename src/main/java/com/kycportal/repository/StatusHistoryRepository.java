package com.kycportal.repository;

import com.kycportal.domain.KycRecord;
import com.kycportal.domain.StatusHistory;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByKycRecordOrderByChangedAtAsc(KycRecord kycRecord);

    /** Latest status changes across all records, with the record loaded for display. */
    @Query("select h from StatusHistory h join fetch h.kycRecord order by h.changedAt desc")
    List<StatusHistory> findRecent(Pageable pageable);
}