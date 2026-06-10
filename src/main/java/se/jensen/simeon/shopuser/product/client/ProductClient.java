package se.jensen.simeon.shopuser.product.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.jensen.simeon.shopuser.product.model.Product;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductClient {
    private final RestClient restClient;
    private final String productServiceUrl = "http://localhost:8081/api/products";

    public ProductClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(productServiceUrl)
                .build();
    }

    public List<Product> getProducts() {
        Product[] products = restClient
                .get()
                .uri(productServiceUrl)
                .retrieve()
                .body(Product[].class);
        if (products == null || products.length == 0) {
            return List.of();
        }
        return Arrays.asList(products);
    }
}
