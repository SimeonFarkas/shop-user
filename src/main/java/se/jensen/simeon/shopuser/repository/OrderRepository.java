package se.jensen.simeon.shopuser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.jensen.simeon.shopuser.model.CustomerOrder;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserEmail(String userEmail);
}
