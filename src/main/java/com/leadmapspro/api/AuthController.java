package com.leadmapspro.api;

import com.leadmapspro.api.dto.AuthMeResponse;
import com.leadmapspro.api.dto.LoginRequest;
import com.leadmapspro.api.dto.RefreshRequest;
import com.leadmapspro.api.dto.RegisterRequest;
import com.leadmapspro.api.dto.TokenResponse;
import com.leadmapspro.security.CurrentUserId;
import com.leadmapspro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserId currentUserId;

    public AuthController(AuthService authService, CurrentUserId currentUserId) {
        this.authService = authService;
        this.currentUserId = currentUserId;
    }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(currentUserId.require(), request);
    }

    @GetMapping("/me")
    public AuthMeResponse me() {
        return authService.me(currentUserId.require());
    }
}
