package com.example.zta.config;

import com.example.zta.user.Role;
import com.example.zta.user.RoleRepository;
import com.example.zta.user.User;
import com.example.zta.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, 
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        if (!roleRepository.findByName("ROLE_USER").isPresent()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }

        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
            admin.setEnabled(true);
            admin.setFailedLogins(0);
            
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            admin.setRoles(Set.of(adminRole));
            
            userRepository.save(admin);
            System.out.println("========================================");
            System.out.println("Admin user created!");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("========================================");
        }
    }
}


