package com.example.Shop.Entity;

import com.example.Shop.Entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @OneToMany(mappedBy ="order" , cascade =CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> orderDetailsList = new ArrayList<>();


}
