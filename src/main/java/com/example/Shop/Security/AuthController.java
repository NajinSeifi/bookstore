package com.example.Shop.Security;

import com.example.Shop.Entity.Role;
import com.example.Shop.Entity.User;
import com.example.Shop.Repository.RoleRepository;
import com.example.Shop.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager1, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager1;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()){
            return "register";
        }
        if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            model.addAttribute("error", "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role defaultRole = roleRepository.findByName("ROLE_USER");
        roles.add(defaultRole);

        user.setAuthorities(roles);

        userRepository.save(user);
        return "redirect:/login";
    }


    @PostMapping("/login")
    public String loginUser (@RequestParam String username , @RequestParam String password , HttpSession session , RedirectAttributes redirectAttributes){
        try {
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticated = authenticationManager.authenticate(authReq);
            SecurityContextHolder.getContext().setAuthentication(authenticated);

            if (authenticated.isAuthenticated()) {
                session.setAttribute("username", username);
                return "redirect: product/index";
            } else {
                redirectAttributes.addFlashAttribute("error", "invalid username or password");
                return "redirect:/login";
            }
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return"redirect:/login";
        }
    }


}
