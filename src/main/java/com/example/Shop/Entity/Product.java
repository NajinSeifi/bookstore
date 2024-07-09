package com.example.Shop.Entity;

import jakarta.persistence.*;
import lombok.Data;

import javax.print.attribute.Attribute;
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


    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    @Column(name="product_count")
    private Integer productCount;

    public Product(String name, double price, String details, byte[] image, String imageUrl, Integer productCount) {
        this.name = name;
        this.price = price;
        this.details = details;
        this.image = image;
        this.imageUrl = imageUrl;
        this.productCount = productCount;

    }


    @ManyToMany
    @JoinTable(
            name="category_product",
            joinColumns = @JoinColumn(name="product_id"),
            inverseJoinColumns = @JoinColumn(name="category_id")
    )
    private Set<Category> categories = new HashSet<>();


    @ManyToMany
    @JoinTable(name="attribute-product",
            joinColumns = @JoinColumn(name = "product_id"))
    private Set<Attributes> attributes = new HashSet<>();


    @ManyToOne
    @JoinColumn(name="author_id")
    private Author author;

    public Product(String name, double price, String details, int productCount, String imageURL) {
    }

    public Product() {

    }

    public void addAttributes(Attributes attribute) {
        attributes.add(attribute);
        attribute.getProducts().add(this);
    }
    public void removeAttributes(Attributes attribute){
        attributes.remove(attribute);
        attribute.getProducts().remove(this);
    }

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
