package com.example.Shop.Controller;

import com.example.Shop.Entity.User;
import com.example.Shop.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/index")
    public String ViewUsers(Model model){
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return"index";
    }
}
