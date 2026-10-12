package com.kycportal.web;

import com.kycportal.domain.AppUser;
import com.kycportal.repository.AppUserRepository;
import com.kycportal.service.WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final AppUserRepository users;
    private final WorkflowService workflow;

    public ReviewController(AppUserRepository users, WorkflowService workflow) {
        this.users = users;
        this.workflow = workflow;
    }

    @PostMapping("/records/{id}/review")
    public String startReview(@PathVariable Long id, Authentication authentication, RedirectAttributes flash) {
        workflow.startReview(id, current(authentication));
        flash.addFlashAttribute("message", "Review started. The record is now under review.");
        return "redirect:/records/" + id;
    }

    @PostMapping("/records/{id}/verify")
    public String verify(@PathVariable Long id, Authentication authentication, RedirectAttributes flash) {
        workflow.verify(id, current(authentication));
        flash.addFlashAttribute("message", "Record verified.");
        return "redirect:/records/" + id;
    }

    @PostMapping("/records/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         Authentication authentication,
                         RedirectAttributes flash) {
        if (reason == null || reason.isBlank()) {
            flash.addFlashAttribute("error", "Enter a reason to reject this record.");
            return "redirect:/records/" + id;
        }
        if (reason.trim().length() > 500) {
            flash.addFlashAttribute("error", "Use 500 characters or fewer for the reason.");
            return "redirect:/records/" + id;
        }
        workflow.reject(id, current(authentication), reason);
        flash.addFlashAttribute("message", "Record rejected. The applicant can edit and resubmit it.");
        return "redirect:/records/" + id;
    }

    private AppUser current(Authentication authentication) {
        return users.findByUsername(authentication.getName()).orElseThrow();
    }
}