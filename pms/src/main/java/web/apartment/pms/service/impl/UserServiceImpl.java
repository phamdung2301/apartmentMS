package web.apartment.pms.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.User;
import web.apartment.pms.repository.UserRepository;
import web.apartment.pms.service.UserService;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User saveUser(User user) {
        // Only encode if it isn't encoded already (e.g. starts with $2a$ or $2b$)
        if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User createUserForTenant(String fullName, String email, String phone) {
        String baseUsername = email.contains("@") ? email.split("@")[0].toLowerCase() : "tenant";
        String username = baseUsername;
        int count = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + count++;
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .username(username)
                .password(passwordEncoder.encode("123456")) // default password is '123456'
                .role("TENANT")
                .build();

        return userRepository.save(user);
    }

    @Override
    public java.util.List<User> searchTenantsByName(String name) {
        return userRepository.searchTenantsByName(name);
    }
}
