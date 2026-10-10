package com.kycportal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kycportal.repository.KycRecordRepository;
import com.kycportal.repository.StatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

@SpringBootTest(properties = "kyc.upload-dir=target/test-uploads")
@AutoConfigureMockMvc
class CreateRecordTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KycRecordRepository records;

    @Autowired
    private StatusHistoryRepository history;

    private RequestBuilder submit(String fullName, String fileName) {
        MockMultipartFile document =
            new MockMultipartFile("document", fileName, "application/pdf", "dummy document".getBytes());
        return multipart("/records")
            .file(document)
            .param("fullName", fullName)
            .param("dateOfBirth", "1999-05-14")
            .param("idType", "PAN")
            .param("idNumber", "ABCDE1234F")
            .with(user("applicant1").roles("APPLICANT"))
            .with(csrf());
    }

    @Test
    void applicantCanCreateRecord() throws Exception {
        long recordsBefore = records.count();
        long historyBefore = history.count();

        mockMvc.perform(submit("Asha Kulkarni", "id-proof.pdf"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", startsWith("/records/")));

        assertEquals(recordsBefore + 1, records.count());
        assertEquals(historyBefore + 1, history.count());
    }

    @Test
    void verifierCannotOpenCreateForm() throws Exception {
        mockMvc.perform(get("/records/new").with(user("verifier1").roles("VERIFIER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void missingNameShowsErrorAndSavesNothing() throws Exception {
        long recordsBefore = records.count();

        mockMvc.perform(submit("", "id-proof.pdf"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Enter the full name")));

        assertEquals(recordsBefore, records.count());
    }

    @Test
    void wrongFileTypeIsRejected() throws Exception {
        long recordsBefore = records.count();

        mockMvc.perform(submit("Asha Kulkarni", "program.exe"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Upload a PDF, JPG or PNG file")));

        assertEquals(recordsBefore, records.count());
    }
}