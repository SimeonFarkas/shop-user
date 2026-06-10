package se.jensen.simeon.shopuser.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.simeon.shopuser.product.client.ProductClient;
import se.jensen.simeon.shopuser.product.model.Product;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductClient productClient;

    public ProductController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productClient.getProducts();
    }
}
