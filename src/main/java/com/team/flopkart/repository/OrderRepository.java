package com.team.flopkart.repository;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user.
     */
    List<Order> findByUser(User user);

    /**
     * Find order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);
}
