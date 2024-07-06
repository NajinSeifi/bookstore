package com.example.Shop.Entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "author" , cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products = new HashSet<>();

    public void addProduct(Product product){
        products.add(product);
        product.setAuthor(this);
    }

    public void removeProduct(Product product){
        products.remove(product);
        product.setAuthor(null);
    }


    public Long getId() {
        return id;
    }

    public String getName(){
        return name;
    }
}
