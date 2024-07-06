package com.example.Shop.Repository;

import com.example.Shop.Entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author,Long> {
}
