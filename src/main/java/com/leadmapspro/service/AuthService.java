package com.leadmapspro.service;

import com.leadmapspro.api.dto.AuthMeResponse;
import com.leadmapspro.api.dto.LoginRequest;
import com.leadmapspro.api.dto.RefreshRequest;
import com.leadmapspro.api.dto.RegisterRequest;
import com.leadmapspro.api.dto.TokenResponse;
import com.leadmapspro.config.LeadMapsProperties;
import com.leadmapspro.domain.AppUser;
import com.leadmapspro.domain.RefreshToken;
import com.leadmapspro.repository.AppUserRepository;
import com.leadmapspro.repository.RefreshTokenRepository;
import com.leadmapspro.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int WELCOME_CREDITS = 50;

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LeadMapsProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            LeadMapsProperties properties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.properties = properties;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setSupabaseUserId("local-" + UUID.randomUUID());
        user.setCreditBalance(WELCOME_CREDITS);
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        AppUser user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        String raw = req.getRefreshToken().trim();
        String hash = sha256Hex(raw);
        RefreshToken stored =
                refreshTokenRepository
                        .findByTokenHashAndRevokedFalse(hash)
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido."));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        AppUser user = stored.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId, RefreshRequest req) {
        if (req.getRefreshToken() == null || req.getRefreshToken().isBlank()) {
            return;
        }
        String hash = sha256Hex(req.getRefreshToken().trim());
        refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .ifPresent(
                        rt -> {
                            if (rt.getUser().getId().equals(userId)) {
                                rt.setRevoked(true);
                                refreshTokenRepository.save(rt);
                            }
                        });
    }

    public AuthMeResponse me(UUID userId) {
        AppUser user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        AuthMeResponse r = new AuthMeResponse();
        r.setUserId(user.getId());
        r.setEmail(user.getEmail());
        r.setCreditBalance(user.getCreditBalance());
        return r;
    }

    private TokenResponse issueTokens(AppUser user) {
        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        long accessSeconds = properties.getJwt().getAccessExpirationMinutes() * 60;

        byte[] rnd = new byte[48];
        secureRandom.nextBytes(rnd);
        String refreshRaw = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd);
        String refreshHash = sha256Hex(refreshRaw);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(refreshHash);
        rt.setExpiresAt(
                Instant.now().plusSeconds(properties.getJwt().getRefreshExpirationDays() * 86400L));
        refreshTokenRepository.save(rt);

        TokenResponse res = new TokenResponse();
        res.setAccessToken(access);
        res.setRefreshToken(refreshRaw);
        res.setExpiresInSeconds(accessSeconds);
        res.setTokenType("Bearer");
        return res;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
