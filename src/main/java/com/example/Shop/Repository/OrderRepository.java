package com.example.Shop.Repository;

import com.example.Shop.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long > {
    List<Order> findByUserId(Long userId);
}
