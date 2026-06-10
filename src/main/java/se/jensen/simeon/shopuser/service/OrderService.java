package se.jensen.simeon.shopuser.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import se.jensen.simeon.shopuser.dto.CreateOrderItemReq;
import se.jensen.simeon.shopuser.dto.CreateOrderReq;
import se.jensen.simeon.shopuser.model.CustomerOrder;
import se.jensen.simeon.shopuser.model.OrderItem;
import se.jensen.simeon.shopuser.product.client.ProductClient;
import se.jensen.simeon.shopuser.product.model.Product;
import se.jensen.simeon.shopuser.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    public CustomerOrder createOrder(String userEmail, CreateOrderReq request) {
        List<Product> products = productClient.getProducts();

        CustomerOrder order = new CustomerOrder(userEmail);

        for (CreateOrderItemReq itemRequest : request.items()) {
            Product product = findProduct(products, itemRequest.productId());

            OrderItem item = new OrderItem(
                    product.id(),
                    product.title(),
                    product.price(),
                    itemRequest.quantity()
            );


            order.addItem(item);
        }
        return orderRepository.save(order);

    }

    public List<CustomerOrder> getOrderForUser(String userEmail) {
        return orderRepository.findByUserEmail(userEmail);
    }

    private Product findProduct(List<Product> products, long productId) {
        return products.stream()
                .filter(product -> product.id() == productId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));
    }
}
