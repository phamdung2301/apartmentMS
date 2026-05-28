package web.apartment.pms.service;

import web.apartment.pms.model.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    User saveUser(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User createUserForTenant(String fullName, String email, String phone);
    java.util.List<User> searchTenantsByName(String name);
}
