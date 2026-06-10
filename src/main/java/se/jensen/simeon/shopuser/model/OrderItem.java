package se.jensen.simeon.shopuser.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long productId;

    private String productTitle;

    private Double price;

    private int quantity;


    public OrderItem() {
    }

    public OrderItem(long productId, String productTitle, Double price, int quantity) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.price = price;
        this.quantity = quantity;
    }
}
