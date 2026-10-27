package com.kycportal.web;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.KycRecord;
import com.kycportal.domain.Role;
import com.kycportal.domain.Status;
import com.kycportal.repository.AppUserRepository;
import com.kycportal.repository.KycRecordRepository;
import com.kycportal.repository.StatusHistoryRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final DateTimeFormatter DATE_TIME =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private final AppUserRepository users;
    private final KycRecordRepository records;
    private final StatusHistoryRepository history;

    public HomeController(AppUserRepository users,
                          KycRecordRepository records,
                          StatusHistoryRepository history) {
        this.users = users;
        this.records = records;
        this.history = history;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        AppUser user = users.findByUsername(authentication.getName()).orElseThrow();
        boolean applicant = user.getRole() == Role.APPLICANT;

        List<StageCount> stages = new ArrayList<>();
        Map<Status, Long> counts = new EnumMap<>(Status.class);
        long total = 0;
        for (Status status : Status.values()) {
            // Applicants only ever see their own numbers.
            long count = applicant
                ? records.countByApplicantAndStatus(user, status)
                : records.countByStatus(status);
            counts.put(status, count);
            total += count;
            stages.add(new StageCount(status, count));
        }
        long verified = counts.get(Status.VERIFIED);
        long decided = verified + counts.get(Status.REJECTED);

        model.addAttribute("user", user);
        model.addAttribute("roleLabel", user.getRole().getLabel());
        model.addAttribute("stages", stages);
        model.addAttribute("total", total);
        model.addAttribute("userCount", users.count());
        model.addAttribute("waitingCount", counts.get(Status.SUBMITTED));
        model.addAttribute("inReviewCount", counts.get(Status.UNDER_REVIEW));
        model.addAttribute("approvalRate",
            decided == 0 ? null : Integer.valueOf((int) Math.round(verified * 100.0 / decided)));

        if (applicant) {
            List<KycRecord> mine = records.findByApplicantOrderByCreatedAtDesc(user);
            model.addAttribute("myRecords", mine.stream().limit(5).toList());
            model.addAttribute("rejectedRecord", mine.stream()
                .filter(r -> r.getStatus() == Status.REJECTED)
                .findFirst()
                .orElse(null));
        } else {
            // Work queue for verifiers and admins: oldest first.
            List<KycRecord> queue = records.findAllWithApplicant().stream()
                .filter(r -> r.getStatus() == Status.SUBMITTED || r.getStatus() == Status.UNDER_REVIEW)
                .sorted(Comparator.comparing(KycRecord::getCreatedAt))
                .limit(8)
                .toList();
            model.addAttribute("queue", queue);
        }

        if (user.getRole() == Role.ADMIN) {
            List<AppUser> everyone = users.findAll();
            model.addAttribute("userList", everyone);
            model.addAttribute("applicantCount", everyone.stream().filter(u -> u.getRole() == Role.APPLICANT).count());
            model.addAttribute("verifierCount", everyone.stream().filter(u -> u.getRole() == Role.VERIFIER).count());
            model.addAttribute("adminCount", everyone.stream().filter(u -> u.getRole() == Role.ADMIN).count());

            List<ActivityLine> activity = history.findRecent(PageRequest.of(0, 8)).stream()
                .map(h -> new ActivityLine(
                    DATE_TIME.format(h.getChangedAt()),
                    h.getKycRecord().getId(),
                    h.getKycRecord().getReferenceNo(),
                    h.getFromStatus() == null
                        ? "Submitted"
                        : h.getFromStatus().getLabel() + " to " + h.getToStatus().getLabel(),
                    h.getChangedBy(),
                    h.getNote()))
                .toList();
            model.addAttribute("activity", activity);
        }
        return "home";
    }
}