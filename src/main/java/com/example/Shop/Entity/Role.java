package com.example.Shop.Entity;

import com.example.Shop.Entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Entity
@Table(name="roles")
@Data
@NoArgsConstructor
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(name ="name")
    private String name;


    @Override
    public String getAuthority() {
        return "ROLE_" + name;
    }
}
