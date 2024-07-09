package com.example.Shop.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attributes")
@Data
@NoArgsConstructor

public class Attributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Attributes(String fictionType) {
        this.fictionType = fictionType;
    }

    private  String fictionType;


    @ManyToMany(mappedBy = "attributes")
    private Set<Product> products = new HashSet<>();
    public void addProduct(Product product) {
        products.add(product);
        product.getAttributes().add(this);
    }


}
