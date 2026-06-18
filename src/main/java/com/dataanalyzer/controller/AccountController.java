package com.dataanalyzer.controller;

import com.dataanalyzer.entity.User;
import com.dataanalyzer.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/account")
    public String showAccountPage(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        model.addAttribute("userEmail", email);
        model.addAttribute("displayName", user != null ? user.getDisplayName() : "");
        return "account";
    }

    @PostMapping("/account/password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  Model model,
                                  Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        model.addAttribute("userEmail", email);
        model.addAttribute("displayName", user != null ? user.getDisplayName() : "");

        if (user == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            return "account";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("success", "Password updated successfully.");
        return "account";
    }
}
