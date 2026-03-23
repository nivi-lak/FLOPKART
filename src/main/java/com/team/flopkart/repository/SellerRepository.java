package com.team.flopkart.repository;

import com.team.flopkart.model.Seller;
import com.team.flopkart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SellerRepository — Member 2.
 *
 * Design Principle: OCP note — new query methods can be added here
 * without touching the Seller entity class itself.
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(User user);

    Optional<Seller> findByUser_Email(String email);

    boolean existsByUser(User user);

    boolean existsByGstNumber(String gstNumber);
}