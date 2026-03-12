package com.goDelivery.goDelivery.modules.auth;

/**
 * Public API boundary for the Auth module.
 *
 * Exposed services:
 *   - AuthenticationService → login, registration, token generation
 *   - OTPService            → OTP generation and verification
 *
 * Exposed DTOs:
 *   - LoginRequest / LoginResponse
 *   - OTPVerificationRequest / OTPVerificationResponse
 *   - ChangePasswordRequest / ForgotPasswordRequest / ResetPasswordRequest
 *
 * Dependencies on other modules:
 *   - shared/security → JwtService (token creation/validation)
 */
public final class AuthModuleApi {
    private AuthModuleApi() {}
}
