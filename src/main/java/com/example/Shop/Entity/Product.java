package com.example.Shop.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    private String details;
    private byte[] image;
    private String imageUrl;
    @Column(name="product_count")
    private Integer productCount;

    @ManyToMany
    @JoinTable(
            name="category_product",
            joinColumns = @JoinColumn(name="product_id"),
            inverseJoinColumns = @JoinColumn(name="category_id")
    )
    private Set<Category> categories = new HashSet<>();



    @ManyToOne
    @JoinColumn(name="author_id")
    private Author author;

    public void addCategory(Category category) {
        categories.add(category);
        category.addProduct(this);
    }
    public void removeCategory(Category category) {
        categories.remove(category);
        category.getProducts().remove(this);
    }
    public void decreaseCount(int amount){
        if(this.productCount !=null){
            this.productCount-=amount;
        }else{
            this.productCount=0;
        }
    }
    public double getPrice() {
        return this.price;
    }

}
