package com.kycportal.web;

import com.kycportal.domain.AppUser;
import com.kycportal.domain.Role;
import com.kycportal.domain.Status;
import com.kycportal.repository.AppUserRepository;
import com.kycportal.repository.KycRecordRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AppUserRepository users;
    private final KycRecordRepository records;

    public HomeController(AppUserRepository users, KycRecordRepository records) {
        this.users = users;
        this.records = records;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        AppUser user = users.findByUsername(authentication.getName()).orElseThrow();

        List<StageCount> stages = new ArrayList<>();
        long total = 0;
        for (Status status : Status.values()) {
            // Applicants only ever see their own numbers.
            long count = user.getRole() == Role.APPLICANT
                ? records.countByApplicantAndStatus(user, status)
                : records.countByStatus(status);
            total += count;
            stages.add(new StageCount(status, count));
        }

        model.addAttribute("user", user);
        model.addAttribute("roleLabel", user.getRole().getLabel());
        model.addAttribute("stages", stages);
        model.addAttribute("total", total);
        model.addAttribute("userCount", users.count());
        return "home";
    }
}
