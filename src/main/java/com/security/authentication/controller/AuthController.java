package com.security.authentication.controller;

import com.security.authentication.dto.auth.Login;
import com.security.authentication.dto.auth.Logout;
import com.security.authentication.dto.auth.Refresh;
import com.security.authentication.dto.auth.Register;
import com.security.authentication.dto.common.ApiResponse;
import com.security.authentication.security.CustomUserDetails;
import com.security.authentication.security.JwtService;
import com.security.authentication.services.AuthService;
import com.security.authentication.util.ResponseFactory;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Endpoint to register a new user.
     *
     * @param registerRequestDTO the registration request data transfer object
     * @return ResponseEntity with registration response
     */
    @PostMapping("/register")
    ResponseEntity<ApiResponse<Register.RegisterResponseDTO>> register(@RequestBody Register.RegisterRequestDTO registerRequestDTO) {
        Register.RegisterResponseDTO response = authService.registerUser(registerRequestDTO);
        return ResponseFactory.ok(response, "Registration successful", "/api/v1/auth/register");
    }

    /**
     * Endpoint to refresh the authentication token.
     *
     * @param loginRequestDTO the token to be refreshed
     * @return ResponseEntity with the new token
     */
    @PostMapping("/login")
    ResponseEntity<ApiResponse<Login.LoginResponseDTO>> login(@RequestBody Login.LoginRequestDTO loginRequestDTO) {
        String identifier = loginRequestDTO.getUsername() + ":" + loginRequestDTO.getUserType();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        identifier,
                        loginRequestDTO.getPassword()
                )
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(identifier, Map.of("usertype", loginRequestDTO.getUserType()));
        String refreshToken = authService.issueRefreshTokenForIdentifier(identifier);

        Login.LoginResponseDTO body = Login.LoginResponseDTO.builder()
                .message("Login successful")
                .userId(userDetails.getId().toString())
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
        return ResponseFactory.ok(body, "Login successful", "/api/v1/auth/login");
    }

    /**
     * Endpoint to refresh the authentication token.
     *
     * @param logoutRequestDTO the request containing the refresh token
     * @return ResponseEntity with the new token
     */
    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Logout.LogoutResponseDTO>> logout(@RequestBody Logout.LogoutRequestDTO logoutRequestDTO, HttpServletRequest request) {
        if (logoutRequestDTO.getRefreshToken() == null || logoutRequestDTO.getRefreshToken().isEmpty()) {
            return ResponseFactory.error(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Refresh token is required",
                    java.util.List.of("Refresh token is required"),
                    "/api/v1/auth/logout"
            );
        }

        // Extract access token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            logoutRequestDTO.setAccessToken(accessToken);
        }

        Logout.LogoutResponseDTO loggedOut = authService.logout(logoutRequestDTO);
        return ResponseFactory.ok(loggedOut, "Logout successful", "/api/v1/auth/logout");
    }

    /**
     * Endpoint to refresh the authentication token.
     *
     * @param req the request containing the refresh token
     * @return ResponseEntity with the new access token
     */
    @PostMapping("/refresh-token")
    ResponseEntity<ApiResponse<Refresh.RefreshResponseDTO>> refresh(@RequestBody Refresh.RefreshRequestDTO req) {
        return ResponseFactory.ok(
                authService.newAccessTokenFromRefresh(req.getRefreshToken()),
                "Access token issued",
                "/api/v1/auth/refresh-token"
        );
    }

    /**
     * Health check endpoint to verify if the authentication service is running.
     *
     * @return ResponseEntity with a health check message
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseFactory.ok("Authentication service is running", "OK", "/api/v1/auth/health");
    }
}
