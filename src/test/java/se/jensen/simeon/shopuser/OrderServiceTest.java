package se.jensen.simeon.shopuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import se.jensen.simeon.shopuser.dto.CreateOrderItemReq;
import se.jensen.simeon.shopuser.dto.CreateOrderReq;
import se.jensen.simeon.shopuser.model.CustomerOrder;
import se.jensen.simeon.shopuser.product.client.ProductClient;
import se.jensen.simeon.shopuser.product.model.Product;
import se.jensen.simeon.shopuser.product.model.Rating;
import se.jensen.simeon.shopuser.repository.OrderRepository;
import se.jensen.simeon.shopuser.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        mockProducts = List.of(
                new Product(1, "Fjallraven Backpack", 109.95, "Description", "clothing", "image.jpg", new Rating(4.0, 100))
        );
    }

    @Test
    void createOrder_shouldCreateOrderWithItems() {
        // Arrange
        String userEmail = "test@test.com";
        CreateOrderItemReq itemReq = new CreateOrderItemReq(1, 2);
        CreateOrderReq request = new CreateOrderReq(List.of(itemReq));

        when(productClient.getProducts()).thenReturn(mockProducts);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerOrder result = orderService.createOrder(userEmail, request);

        // Assert
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(1, result.getItems().size());
        assertEquals("Fjallraven Backpack", result.getItems().get(0).getProductTitle());
        assertEquals(2, result.getItems().get(0).getQuantity());
        assertEquals("CREATED", result.getStatus());
    }

    @Test
    void createOrder_shouldThrowWhenProductNotFound() {
        // Arrange
        String userEmail = "test@test.com";
        CreateOrderItemReq itemReq = new CreateOrderItemReq(999, 1);
        CreateOrderReq request = new CreateOrderReq(List.of(itemReq));

        when(productClient.getProducts()).thenReturn(mockProducts);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () ->
                orderService.createOrder(userEmail, request)
        );
    }

    @Test
    void getOrderForUser_shouldReturnUserOrders() {
        // Arrange
        String userEmail = "test@test.com";
        CustomerOrder order = new CustomerOrder(userEmail);
        when(orderRepository.findByUserEmail(userEmail)).thenReturn(List.of(order));

        // Act
        List<CustomerOrder> result = orderService.getOrderForUser(userEmail);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userEmail, result.get(0).getUserEmail());
    }
}

