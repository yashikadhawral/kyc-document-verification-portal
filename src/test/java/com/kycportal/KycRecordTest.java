package com.kycportal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kycportal.domain.KycRecord;
import org.junit.jupiter.api.Test;

class KycRecordTest {

    @Test
    void idNumberIsMaskedExceptLastFour() {
        KycRecord record = new KycRecord();
        record.setIdNumber("ABCDE1234F");
        assertEquals("******234F", record.getMaskedIdNumber());
    }

    @Test
    void shortIdNumberIsFullyMasked() {
        KycRecord record = new KycRecord();
        record.setIdNumber("123");
        assertEquals("***", record.getMaskedIdNumber());
    }
}
