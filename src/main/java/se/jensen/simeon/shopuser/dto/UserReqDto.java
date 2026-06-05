package se.jensen.simeon.shopuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO för att ta emot användarinformation av klienten.
 * UserReqDto efterfrågar inte mer info av klienten än nödvändigt.
 *
 * @author Simeon
 * Dokumenterad: 2026-05-24
 */
public record UserReqDto(
        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
        String username,

        @NotBlank(message = "Email adress is required.")
        @Email(message = "Please provide a valid email.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 20 characters.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,255}$",
                message = "Password must contain at least one uppercase letter and one number"
        )
        String password
) {
}
