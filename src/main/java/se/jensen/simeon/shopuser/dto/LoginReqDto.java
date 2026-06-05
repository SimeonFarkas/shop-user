package se.jensen.simeon.shopuser.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inloggningsuppgifter som skickas från klienten till servern.
 *
 * @author Simeon
 * Dokumenterad: 2026-06-02
 */
public record LoginReqDto(
        @NotBlank(message = "Please enter a valid username or email.")
        String usernameOrEmail,

        @NotBlank(message = "Password is required.")
        String password
) {
}
