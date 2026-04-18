package com.team.flopkart.repository;

import com.team.flopkart.model.OrderItem;
import com.team.flopkart.model.Seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * Repository for OrderItem entities.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductSeller(Seller seller);

}
