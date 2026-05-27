package com.elfaddoui.backend.config;

import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminBootstrap(
            AppProperties appProperties,
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        String email = normalize(appProperties.getAdmin().getEmail());
        String password = appProperties.getAdmin().getPassword();

        if (email.isBlank() || password == null || password.isBlank()) {
            return;
        }

        String fullName = appProperties.getAdmin().getFullName();

        User user = userRepository.findByEmail(email).orElseGet(() ->
                new User(fullName == null || fullName.isBlank() ? "Store Admin" : fullName.trim(), email, "")
        );

        user.setFullName(fullName == null || fullName.isBlank() ? "Store Admin" : fullName.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);

        HashSet<Role> roles = new HashSet<>(user.getRoles());
        roles.add(Role.ADMIN);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("Admin account ready for {}", email);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
