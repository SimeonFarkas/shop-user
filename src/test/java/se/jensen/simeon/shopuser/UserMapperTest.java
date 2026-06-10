package se.jensen.simeon.shopuser;

import org.junit.jupiter.api.Test;
import se.jensen.simeon.shopuser.dto.UserRespDto;
import se.jensen.simeon.shopuser.mapper.UserMapper;
import se.jensen.simeon.shopuser.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    public void testUserToDto() {
        //Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("Handsome_Henry");
        user.setEmail("henry@veryhandsome.com");
        user.setRole("USER");

        //Act - Här sker mappningen
        UserRespDto dto = userMapper.userToDto(user);

        //Assert - Kontrollera att det blev rätt
        assertNotNull(dto, "Mapped DTO shouldn't be null");
        assertEquals(1L, dto.id(), "ID doesn't match");
        assertEquals("Handsome_Henry", dto.username(), "Username doesn't match");
        assertEquals("henry@veryhandsome.com", dto.email(), "E-mail doesn't match");
        assertEquals("USER", dto.role(), "Role doesn't match");
    }
}
