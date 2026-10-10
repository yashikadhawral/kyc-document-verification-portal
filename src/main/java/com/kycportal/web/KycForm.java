package com.kycportal.web;

import com.kycportal.domain.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class KycForm {

    @NotBlank(message = "Enter the full name as it appears on the ID")
    @Size(max = 100, message = "Use 100 characters or fewer")
    private String fullName;

    @NotNull(message = "Enter the date of birth")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull(message = "Choose an ID type")
    private IdType idType;

    @NotBlank(message = "Enter the ID number")
    @Pattern(regexp = "^([A-Za-z0-9]{4,30})?$", message = "Use 4 to 30 letters or digits, with no spaces")
    private String idNumber;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public IdType getIdType() { return idType; }
    public void setIdType(IdType idType) { this.idType = idType; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
}