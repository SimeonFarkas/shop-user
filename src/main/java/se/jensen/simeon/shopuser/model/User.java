package se.jensen.simeon.shopuser.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Returnerar användarens behörigheter/roller
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // Returnerar lösenordet
    @Override
    public String getPassword() {
        return password;
    }

    // Returnerar användarnamnet
    @Override
    public String getUsername() {
        return username;
    }

    // Är kontot inte utgånget?
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Är kontot inte låst?
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Är lösenordet inte utgånget?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Är kontot aktiverat?
    @Override
    public boolean isEnabled() {
        return true;
    }
}
