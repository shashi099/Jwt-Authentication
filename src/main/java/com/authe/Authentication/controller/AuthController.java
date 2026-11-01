package com.authe.Authentication.controller;

import com.authe.Authentication.dto.*;
import com.authe.Authentication.entity.User;
import com.authe.Authentication.repo.UserRepo;
import com.authe.Authentication.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    // 1. REGISTER ENDPOINT
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Ensure format is ROLE_USER or ROLE_ADMIN
        String formattedRole = request.getRole().toUpperCase().startsWith("ROLE_")
                ? request.getRole().toUpperCase()
                : "ROLE_" + request.getRole().toUpperCase();
        user.setRole(formattedRole);

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully with role: " + formattedRole);
    }

    // 2. LOGIN ENDPOINT (Returns token & role so frontend knows which dashboard to show)
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(new JwtResponse(token, role));
    }

    // 3. USER DASHBOARD ENDPOINT (Only accessible if token has ROLE_USER)
    @GetMapping("/user/dashboard")
    public ResponseEntity<String> userDashboard() {
        return ResponseEntity.ok("Welcome to the USER Dashboard! 🚀 (Restricted to Users)");
    }

    // 4. ADMIN DASHBOARD ENDPOINT (Only accessible if token has ROLE_ADMIN)
    @GetMapping("/admin/dashboard")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome to the ADMIN Control Panel! 🛡️ (Restricted to Admins)");
    }
}