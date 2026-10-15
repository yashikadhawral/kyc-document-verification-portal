package com.kycportal.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.IdType;
import com.kycportal.domain.KycRecord;
import com.kycportal.repository.AppUserRepository;
import com.kycportal.repository.KycRecordRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;

class UserJourneysTest extends BrowserTest {

    @Autowired
    private AppUserRepository users;

    @Autowired
    private KycRecordRepository records;

    private void signIn(String username, String pass) {
        driver.get(url("/login"));
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.id("login-button")).click();
    }

    // Journey 1: a valid login shows the overview for that user
    @Test
    void validLoginShowsOverview() {
        signIn("applicant1", password);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("welcome"), "Welcome"));
        assertEquals("Welcome, Asha Kulkarni", driver.findElement(By.id("welcome")).getText());
    }

    // Journey 2: a wrong password shows an error and stays on the sign-in page
    @Test
    void wrongPasswordShowsError() {
        signIn("applicant1", "not-the-password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-error")));
        assertTrue(error.getText().contains("Wrong username or password"));
    }

    // Journey 3: an applicant submits an application and sees it as Submitted
    @Test
    void applicantSubmitsApplication() throws Exception {
        signIn("applicant1", password);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
        driver.get(url("/records/new"));

        driver.findElement(By.id("fullName")).sendKeys("Asha Kulkarni");
        WebElement dob = driver.findElement(By.id("dateOfBirth"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]", dob, "1999-05-14");
        new Select(driver.findElement(By.id("idType"))).selectByValue("PAN");
        driver.findElement(By.id("idNumber")).sendKeys("ABCDE1234F");

        Path pdf = Files.createTempFile("proof", ".pdf");
        Files.writeString(pdf, "dummy document");
        driver.findElement(By.id("document")).sendKeys(pdf.toAbsolutePath().toString());
        driver.findElement(By.id("submit-application")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("status-stamp")));
        assertEquals("Submitted", driver.findElement(By.id("status-stamp")).getText());
        assertTrue(driver.findElement(By.id("reference")).getText().startsWith("Reference KYC-"));
    }

    // Journey 4: a verifier cannot open the create page
    @Test
    void verifierIsBlockedFromCreatingRecords() {
        signIn("verifier1", password);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
        driver.get(url("/records/new"));
        assertEquals("You don't have access to this page", driver.findElement(By.tagName("h1")).getText());
    }

    // Journey 5: a verifier starts a review and verifies a submitted record
    @Test
    void verifierVerifiesARecord() {
        AppUser applicant = users.findByUsername("applicant1").orElseThrow();
        KycRecord record = new KycRecord();
        record.setApplicant(applicant);
        record.setReferenceNo("KYC-S-" + (System.nanoTime() % 100000000L));
        record.setFullName("Selenium Review Case");
        record.setDateOfBirth(LocalDate.of(1999, 5, 14));
        record.setIdType(IdType.PAN);
        record.setIdNumber("ABCDE1234F");
        record.setDocumentName("proof.pdf");
        record.setDocumentPath("none.pdf");
        Long id = records.save(record).getId();

        signIn("verifier1", password);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
        driver.get(url("/records/" + id));

        wait.until(ExpectedConditions.elementToBeClickable(By.id("start-review"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("verify-button"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("status-stamp"), "Verified"));
        assertEquals("Verified", driver.findElement(By.id("status-stamp")).getText());
    }
}