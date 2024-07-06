package com.example.Shop.Repository;

import com.example.Shop.Entity.Category;
import com.example.Shop.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
