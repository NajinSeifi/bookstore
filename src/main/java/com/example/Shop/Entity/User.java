package com.example.Shop.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
public class User implements UserDetailsService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private int phone;
    private String address;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="user_role",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name="role_id")}
    )
    private Set<Role> authorities = new HashSet<>();


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @OneToMany(mappedBy = "user" , cascade =CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders;




}
