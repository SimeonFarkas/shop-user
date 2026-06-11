package se.jensen.simeon.shopuser.product.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.jensen.simeon.shopuser.product.model.Product;
import se.jensen.simeon.shopuser.security.JwtUtil;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductClient {
    private final RestClient restClient;
    private final JwtUtil jwtUtil;

    public ProductClient(RestClient.Builder builder,
                         JwtUtil jwtUtil,
                         @Value("${product-service.url}") String productServiceUrl) {
        this.restClient = builder
                .baseUrl(productServiceUrl)
                .build();
        this.jwtUtil = jwtUtil;
    }

    public List<Product> getProducts() {
        String token = jwtUtil.generateServiceToken();

        var request = restClient
                .get()
                .uri("/api/products");

        var requestWithHeader = request.header("Authorization", "Bearer " + token);

        Product[] products = requestWithHeader
                .retrieve()
                .body(Product[].class);

        if (products == null || products.length == 0) {
            return List.of();
        }
        return Arrays.asList(products);
    }
}
