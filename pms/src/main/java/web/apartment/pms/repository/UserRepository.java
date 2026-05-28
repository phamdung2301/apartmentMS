package web.apartment.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.apartment.pms.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = 'TENANT' AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<User> searchTenantsByName(@org.springframework.data.repository.query.Param("name") String name);
}
