package com.example.Shop.Repository;

import com.example.Shop.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);

}
