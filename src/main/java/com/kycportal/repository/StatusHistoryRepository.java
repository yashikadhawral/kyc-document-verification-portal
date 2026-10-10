package com.kycportal.repository;

import com.kycportal.domain.KycRecord;
import com.kycportal.domain.StatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByKycRecordOrderByChangedAtAsc(KycRecord kycRecord);
}
