package se.jensen.simeon.shopuser.product.model;

public record Product(
        int id,
        String title,
        double price,
        String description,
        String category,
        String image,
        Rating rating
) {
}
