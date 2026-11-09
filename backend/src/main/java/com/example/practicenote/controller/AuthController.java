package com.example.practicenote.controller;

import com.example.practicenote.dto.AuthResponse;
import com.example.practicenote.dto.LoginRequest;
import com.example.practicenote.dto.RegisterRequest;
import com.example.practicenote.entity.AppUser;
import com.example.practicenote.exception.LoginIdAlreadyExistsException;
import com.example.practicenote.security.AccountPrincipal;
import com.example.practicenote.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accounts;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(AccountService accounts, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy,
                          CsrfTokenRepository csrfTokenRepository) {
        this.accounts = accounts;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
                return ResponseEntity.badRequest().body(Map.of("message", "パスワードはUTF-8で72バイト以内にしてください"));
            }
            AppUser user = accounts.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
        } catch (LoginIdAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.loginId().trim().toLowerCase(java.util.Locale.ROOT), request.password()));

            servletRequest.getSession(true);
            sessionAuthenticationStrategy.onAuthentication(authentication, servletRequest, servletResponse);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, servletRequest, servletResponse);
            csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

            return ResponseEntity.ok(toResponse((AccountPrincipal) authentication.getPrincipal()));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "ログインIDまたはパスワードが違います"));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "ログインできませんでした"));
        }
    }

    @GetMapping("/me")
    public AuthResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal AccountPrincipal user) {
        return toResponse(user);
    }

    private static AuthResponse toResponse(AppUser user) {
        return new AuthResponse(user.getUserId(), user.getLoginId(), user.getUserName(), user.getRole().name());
    }

    private static AuthResponse toResponse(AccountPrincipal user) {
        return new AuthResponse(user.getUserId(), user.getLoginId(), user.getUserName(), user.getRole().name());
    }
}
