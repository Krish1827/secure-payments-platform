package ca.portfolio.payments.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank @Size(min = 10, max = 72) String password) {}
    public record LoginRequest(@Email @NotBlank String email,
                               @NotBlank String password) {}
    public record TokenResponse(String token, String tokenType, String email) {}
}
