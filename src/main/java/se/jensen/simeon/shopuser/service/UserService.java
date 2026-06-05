package se.jensen.simeon.shopuser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.jensen.simeon.shopuser.dto.UserReqDto;
import se.jensen.simeon.shopuser.dto.UserRespDto;
import se.jensen.simeon.shopuser.mapper.UserMapper;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.repository.UserRepository;

@Service
// Lombok genererar konstruktorn automatiskt för alla final-fält
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * UserService innehåller alla metoder vi behöver anropa i UserController.
     * Den innehåller alltså affärslogiken mellan UserControllern och databasen.
     * Klassen annoteras med Service, vilket gör att Spring skapar en Singleton som kan injiceras i andra klasser.
     * <p>
     * Transactional gör att metoderna körs inom en databastransaktion.
     * Antingen lyckas alla metoder med annotering eller ingen av dem.
     *
     * @author Simeon
     * Dokumenterad: 2026-05-24
     */
    @Transactional
    public UserRespDto registerUser(UserReqDto userReqDto) {
        if (userRepository.findByUsername(userReqDto.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(userReqDto.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail already exists");
        }

        User user = new User();
        user.setUsername(userReqDto.username());
        user.setEmail(userReqDto.email());
        user.setPassword(passwordEncoder.encode(userReqDto.password()));
        user.setRole("USER");

        //Spara användaren och sen hämta från databasen så att ID följer med.
        User savedUser = userRepository.save(user);
        return userMapper.userToDto(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Användaren hittades inte"));
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
