package se.jensen.simeon.shopuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.simeon.shopuser.controller.OrderController;
import se.jensen.simeon.shopuser.dto.CreateOrderItemReq;
import se.jensen.simeon.shopuser.dto.CreateOrderReq;
import se.jensen.simeon.shopuser.model.CustomerOrder;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.repository.UserRepository;
import se.jensen.simeon.shopuser.security.JwtUtil;
import se.jensen.simeon.shopuser.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setRole("USER");
    }

    @Test
    @WithMockUser
    void createOrder_shouldReturnOrder() throws Exception {
        // Arrange
        CreateOrderItemReq itemReq = new CreateOrderItemReq(1, 2);
        CreateOrderReq request = new CreateOrderReq(List.of(itemReq));

        CustomerOrder order = new CustomerOrder("test@test.com");

        when(orderService.getOrderForUser("test@test.com")).thenReturn(List.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .with(user(mockUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("test@test.com"));
    }

    @Test
    @WithMockUser
    void getOrders_shouldReturnOrderList() throws Exception {
        // Arrange
        CustomerOrder order = new CustomerOrder("test@test.com");
        when(orderService.getOrderForUser(any())).thenReturn(List.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("test@test.com"));
    }
}

