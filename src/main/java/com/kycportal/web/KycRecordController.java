package com.kycportal.web;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.IdType;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Role;
import com.kycportal.repository.AppUserRepository;
import com.kycportal.repository.KycRecordRepository;
import com.kycportal.repository.StatusHistoryRepository;
import com.kycportal.service.FileStorageService;
import com.kycportal.service.KycService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class KycRecordController {

    private static final DateTimeFormatter DATE =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private final AppUserRepository users;
    private final KycRecordRepository records;
    private final StatusHistoryRepository history;
    private final KycService service;
    private final FileStorageService storage;

    public KycRecordController(AppUserRepository users,
                               KycRecordRepository records,
                               StatusHistoryRepository history,
                               KycService service,
                               FileStorageService storage) {
        this.users = users;
        this.records = records;
        this.history = history;
        this.service = service;
        this.storage = storage;
    }

    @GetMapping("/records/new")
    public String newForm(Authentication authentication, Model model) {
        prepare(model, authentication);
        model.addAttribute("form", new KycForm());
        return "records/new";
    }

    @PostMapping("/records")
    public String create(@Valid @ModelAttribute("form") KycForm form,
                         BindingResult result,
                         @RequestParam("document") MultipartFile document,
                         Authentication authentication,
                         Model model) throws IOException {
        AppUser user = prepare(model, authentication);
        String fileError = storage.validate(document);
        if (result.hasErrors() || fileError != null) {
            model.addAttribute("fileError", fileError);
            return "records/new";
        }
        KycRecord record = service.create(user, form, document);
        return "redirect:/records/" + record.getId() + "?created";
    }

    @GetMapping("/records/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        AppUser user = prepare(model, authentication);
        KycRecord record = records.findWithApplicantById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean owner = record.getApplicant().getId().equals(user.getId());
        if (user.getRole() == Role.APPLICANT && !owner) {
            throw new AccessDeniedException("Applicants can only open their own records");
        }

        List<HistoryLine> lines = history.findByKycRecordOrderByChangedAtAsc(record).stream()
            .map(h -> new HistoryLine(
                DATE_TIME.format(h.getChangedAt()),
                h.getFromStatus() == null
                    ? "Submitted"
                    : h.getFromStatus().getLabel() + " to " + h.getToStatus().getLabel(),
                h.getChangedBy(),
                h.getNote()))
            .toList();

        model.addAttribute("record", record);
        model.addAttribute("dateOfBirth", DATE.format(record.getDateOfBirth()));
        model.addAttribute("history", lines);
        return "records/detail";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fileTooLarge(Authentication authentication, Model model) {
        prepare(model, authentication);
        model.addAttribute("form", new KycForm());
        model.addAttribute("fileError", "The file is larger than 5 MB");
        return "records/new";
    }

    private AppUser prepare(Model model, Authentication authentication) {
        AppUser user = users.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("roleLabel", user.getRole().getLabel());
        model.addAttribute("idTypes", IdType.values());
        return user;
    }
}