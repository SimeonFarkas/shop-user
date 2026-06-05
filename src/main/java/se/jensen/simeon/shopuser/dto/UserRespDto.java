package se.jensen.simeon.shopuser.dto;

/**
 * DTO för att returnera användarinformation till klienten.
 * UserRespDto innehåller inte mer information än nödvändigt, skyddar känslig data.
 *
 * @author Simeon
 * Dokumenterad: 2026-05-24
 */
public record UserRespDto(Long id, String username, String email, String role) {
}
