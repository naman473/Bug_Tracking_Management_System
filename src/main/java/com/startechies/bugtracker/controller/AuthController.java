package com.startechies.bugtracker.controller;

import com.startechies.bugtracker.model.ChangePasswordResult;
import com.startechies.bugtracker.model.RegistrationResult;
import com.startechies.bugtracker.service.AuthService;
import com.startechies.bugtracker.service.ImportUserService;
import com.startechies.bugtracker.util.PasswordValidator;
import com.startechies.bugtracker.util.Routes;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService       authService;
    private final ImportUserService importUserService;

    public AuthController(AuthService authService, ImportUserService importUserService) {
        this.authService       = authService;
        this.importUserService = importUserService;
    }

    @GetMapping(Routes.LOGIN)
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("errMessage", "Invalid email or password.");
        return "login";
    }

    @GetMapping(Routes.REGISTER)
    public String registerPage() { return "register"; }

    @GetMapping(Routes.IMPORT_USERS)
    public String importUsersPage() { return "import-users"; }

    @PostMapping(Routes.REGISTER)
    public String register(@RequestParam String email,
                           @RequestParam String role,
                           @RequestParam String psw,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (!PasswordValidator.isStrong(psw)) {
            model.addAttribute("message", PasswordValidator.RULE_MESSAGE);
            model.addAttribute("msgType", "error");
            return "register";
        }

        RegistrationResult result = authService.register(email, role, psw);
        return switch (result) {
            case RegistrationResult.Success s -> {
                // PRG pattern: redirect so refresh doesn't re-submit the form
                redirectAttributes.addFlashAttribute("message", "Registration successful! Please log in.");
                redirectAttributes.addFlashAttribute("msgType", "success");
                yield Routes.redirect(Routes.LOGIN);
            }
            case RegistrationResult.Failure f -> {
                model.addAttribute("message", f.reason());
                model.addAttribute("msgType", "error");
                yield "register";
            }
        };
    }

    @PostMapping(value = Routes.IMPORT_USERS, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importUsers(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            int count = importUserService.importUsers(file);
            redirectAttributes.addFlashAttribute("message",
                    "Imported " + count + " users successfully.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("message",
                    "Import failed: " + ex.getMessage());
        }
        return Routes.redirect(Routes.IMPORT_USERS);
    }

    @GetMapping(Routes.CHANGE_PASSWORD)
    public String changePasswordPage() { return "change-password"; }

    @PostMapping(Routes.CHANGE_PASSWORD)
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return Routes.redirect(Routes.CHANGE_PASSWORD);
        }
        if (!PasswordValidator.isStrong(newPassword)) {
            redirectAttributes.addFlashAttribute("error", PasswordValidator.RULE_MESSAGE);
            return Routes.redirect(Routes.CHANGE_PASSWORD);
        }
        ChangePasswordResult result = authService.changePassword(
                authentication.getName(), currentPassword, newPassword);
        return switch (result) {
            case ChangePasswordResult.Success s -> {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
                yield Routes.redirect(Routes.CHANGE_PASSWORD);
            }
            case ChangePasswordResult.Failure f -> {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
                yield Routes.redirect(Routes.CHANGE_PASSWORD);
            }
        };
    }
}
