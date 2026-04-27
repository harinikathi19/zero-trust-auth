package com.example.zta.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User createUser(String username, String password, Set<String> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(false); // keep disabled by default
        user.setFailedLogins(0);

        // Convert role names to Role entities safely
        Set<Role> roleEntities = roles.stream()
                .map(roleRepository::findByName)     // Optional<Role>
                .filter(Optional::isPresent)         // keep only existing roles
                .map(Optional::get)                  // unwrap Optional<Role>
                .collect(Collectors.toSet());

        user.setRoles(roleEntities);

        return userRepository.save(user);
    }

    public void updateLoginSuccess(User user, String deviceHash, String ip) {
        user.setFailedLogins(0);
        user.setLastDeviceHash(deviceHash);
        user.setLastIp(ip);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void incrementFailedLogin(String username) {
        User user = findByUsername(username);
        if (user != null) {
            user.setFailedLogins(user.getFailedLogins() + 1);
            userRepository.save(user);
        }
    }

    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // ✅ Spring Security integration
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled()) // disabled users cannot login
                .authorities(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();
    }
}
