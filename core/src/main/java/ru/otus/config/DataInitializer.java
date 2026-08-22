package ru.otus.config;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;
import ru.otus.repositories.UserRepository;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        var adminUsernames = List.of(Pair.of("admin", "admin"));

        adminUsernames.forEach(username -> {
            var user = userRepository.findByUsername(username.getKey());
            if (user.isEmpty()) {
                userRepository.save(User.builder()
                    .firstName(username.getKey())
                    .lastName(username.getKey())
                    .middleName(username.getKey())
                    .roles(Set.of(UserRole.ADMIN))
                    .username(username.getKey())
                    .email("%s@mail.ru".formatted(username.getKey()))
                    .password(passwordEncoder.encode(username.getValue())).build());
            }
        });
    }
}
