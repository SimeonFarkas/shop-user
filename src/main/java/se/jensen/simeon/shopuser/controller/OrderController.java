package se.jensen.simeon.shopuser.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.jensen.simeon.shopuser.dto.CreateOrderReq;
import se.jensen.simeon.shopuser.model.CustomerOrder;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public CustomerOrder createOrder(Authentication authentication, @RequestBody @Valid CreateOrderReq request) {

        if (authentication == null) {
            throw new RuntimeException("User is not authenticated");
        }

        String userEmail = ((User) authentication.getPrincipal()).getEmail();
        return orderService.createOrder(userEmail, request);
    }


    @GetMapping
    public List<CustomerOrder> getOrders(Authentication authentication) {
        String userEmail = ((User) authentication.getPrincipal()).getEmail();
        return orderService.getOrderForUser(userEmail);
    }
}
