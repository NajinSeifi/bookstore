package com.example.Shop.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="orderdetails")
@Data
public class OrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name="product_id", nullable=false)
    private Product product;



    }

