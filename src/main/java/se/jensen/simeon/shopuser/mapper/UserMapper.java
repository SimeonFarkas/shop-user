package se.jensen.simeon.shopuser.mapper;

import org.springframework.stereotype.Component;
import se.jensen.simeon.shopuser.dto.UserRespDto;
import se.jensen.simeon.shopuser.model.User;

/**
 * Klassen mappar/konverterar mellan User-entity och User-DTO:er.
 * Detta för att minimera boilerplate-kod i UserService och skapar med säkerhet genom att returnera User-DTO:er.
 *
 * @author Simeon
 * Dokumenterad: 2026-05-25
 */
@Component
public class UserMapper {

    public UserRespDto userToDto(User user) {
        return new UserRespDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
