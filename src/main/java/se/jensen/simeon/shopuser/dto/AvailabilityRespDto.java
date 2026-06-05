package se.jensen.simeon.shopuser.dto;

/**
 * Används för att kontrollera ifall ett alias eller mejl är tillgängligt i AuthController.
 *
 * @param available true om alias/mejl är ledigt, false om det är upptaget (detta sker i AuthController).
 */
public record AvailabilityRespDto(
        boolean available
) {
}
