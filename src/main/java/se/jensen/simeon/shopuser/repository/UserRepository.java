package se.jensen.simeon.shopuser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.jensen.simeon.shopuser.model.User;

import java.util.Optional;

/**
 * UserRepository ärver metoder från Spring Data JPA.
 * Med "extends JpaRepository" undviker vi boilerplate-kod och ärver:
 * save(), findAll(), findById() osv.
 * Vi får alltså färdiga metoder för att kommunicera med databasen.
 *
 * @author Simeon
 * Dokumenterad: 2026-05-23
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * @return User-objekt med alias som matchar parametern eller null.
     */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
