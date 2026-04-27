package com.example.zta;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.zta.user.Role;
import com.example.zta.user.RoleRepository;

@SpringBootApplication
public class ZtaApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZtaApplication.class, args);
  }

@Bean
CommandLineRunner initRoles(RoleRepository roleRepo) {
    return args -> {
        if (roleRepo.findByName("ROLE_USER").isEmpty()) {
            roleRepo.save(new Role(null, "ROLE_USER"));
        }
        if (roleRepo.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepo.save(new Role(null, "ROLE_ADMIN"));
        }
    };
}
}
