package se.jensen.simeon.shopuser.dto;

/**
 * Data som skickas till klienten vid lyckad inloggning.
 *
 * @param token Den JWT-token som genereras vid lyckad inloggning.
 * @param user  Användarens information.
 * @author Simeon
 * Dokumenterad: 2026-06-02
 */
public record LoginRespDto(
        String token,
        UserRespDto user
) {
}
