package se.jensen.simeon.shopuser.dto;

import jakarta.validation.constraints.Min;

public record CreateOrderItemReq(
        @Min(1)
        long productId,
        @Min(1)
        int quantity
) {
}
