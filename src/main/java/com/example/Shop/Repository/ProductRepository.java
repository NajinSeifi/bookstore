package com.example.Shop.Repository;

import com.example.Shop.Entity.Category;
import com.example.Shop.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategories(Optional<Category> category);


    List<Product> findByPriceBetween(double minPrice, double maxPrice);

    List<Product> findByCategoriesIn(Set<Category> categories);

    @Query("SELECT p.categories FROM Product p WHERE p.id = :id")
    Set<Category> findCategoriesByProductId(@Param("id") Long id);
}


