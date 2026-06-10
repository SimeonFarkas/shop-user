package se.jensen.simeon.shopuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.jensen.simeon.shopuser.dto.UserReqDto;
import se.jensen.simeon.shopuser.dto.UserRespDto;
import se.jensen.simeon.shopuser.mapper.UserMapper;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.repository.UserRepository;
import se.jensen.simeon.shopuser.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Berätta för JUnit att vi använder Mockito
public class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Skapar en fejkad databas-koppling

    @Mock
    private PasswordEncoder passwordEncoder; // Skapar en fejkad kryptering

    @Mock
    private UserMapper userMapper; // Skapar en fejkad mapper

    @InjectMocks
    private UserService userService; // Mockito skapar automatiskt denna och skickar in de 3 mockarna ovan!

    private User testUser;

    @BeforeEach
    void setUp() {
        // Allmän Arrange: Denna användare skapas automatiskt inför VARJE test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Handsome_Henry");
        testUser.setEmail("henry@veryhandsome.com");
        testUser.setPassword("hashed_password");
        testUser.setRole("USER");
    }

    @Test
    public void registerUser_Success() {
        //1. Arrange (Testspecifik setup)
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsome.com", "password");
        UserRespDto expectedResp = new UserRespDto(1L, "Handsome_Henry", "henry@veryhandsome.com", "USER");

        // HÄR KNYTER VI IHOP ALLT: Vi programmerar våra @Mocks att använda objekten!
        // Säg till databas-mocken: "När servicen kollar om namnet/mailet finns, svara att det är ledigt"
        when(userRepository.findByUsername(reqDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(reqDto.email())).thenReturn(Optional.empty());

        // Säg till krypterings-mocken: "När servicen krypterar 'password123', returnera 'hashed_password'"
        when(passwordEncoder.encode(reqDto.password())).thenReturn("hashed_password");

        // Säg till databas-mocken: "När servicen sparar en användare, returnera vår 'testUser' från @BeforeEach"
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Säg till mapper-mocken: "När servicen vill mappa om vår 'testUser', returnera ditt 'expectedResp'"
        when(userMapper.userToDto(testUser)).thenReturn(expectedResp);

        //2. Act - Kör metiden vi testar
        UserRespDto actualResp = userService.registerUser(reqDto);

        //3. Assert - Kontrollera att vi fick exakt det expectedResp vi programmerade mappen att ge
        assertNotNull(actualResp, "Response should not be null");
        assertEquals(expectedResp, actualResp);

        // SÄKERHETSKONTROLL: Verifiera att lösenordet faktiskt skickades till kryptering!
        verify(passwordEncoder, times(1)).encode(reqDto.password());
    }

    @Test
    public void registerUser_ThrowsException_WhenUsernameExists() {
        // 1. Arrange (Testspecifik setup)
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsome.com", "password");

        // Säg till databas-mocken att användarnamnet redan FINNS (returnerar ett paket med vår testUser)
        when(userRepository.findByUsername(reqDto.username())).thenReturn(Optional.of(testUser));

        // 2 & 3. Act & Assert (När vi testar exceptions i JUnit 5 bakar vi ihop dem så här)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(reqDto);
        }, "Borde ha kastat ett fel eftersom användarnamnet redan existerar");

        // Kontrollera att felmeddelandet stämmer överens med din källkod
        assertEquals("Username already exists", exception.getMessage());

        // Verifiera att koden stannade direkt och ALDRIG försökte spara användaren i databasen
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void registerUser_ThrowsException_WhenEmailExists() {
        // 1. Arrange (Testspecifik setup)
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsom.com", "password");

        // Säg till databas-mocken att namnet är ledigt, men att e-postadressen redan FINNS
        when(userRepository.findByUsername(reqDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(reqDto.email())).thenReturn(Optional.of(testUser));

        // 2 & 3. Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(reqDto);
        }, "Borde ha kastat ett fel eftersom e-postadressen redan existerar");

        assertEquals("E-mail already exists", exception.getMessage());

        // Verifiera att koden stannade och ALDRIG försökte spara något
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void registerUser_ThrowsException_WhenDatabaseFailsToSave() {
        // 1. Arrange (Testspecifik setup)
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsome.com", "password");

        // Säg till databasen att namnet och mailet är lediga
        when(userRepository.findByUsername(reqDto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(reqDto.email())).thenReturn(Optional.empty());

        // Krypteringen fungerar som vanligt
        when(passwordEncoder.encode(reqDto.password())).thenReturn("hashed_password");

        // HÄR KASTAR VI FELET: Vi säger till mocken att kasta ett databasfel (t.ex. DataIntegrityViolationException) när save() anropas
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Databasen är offline eller unik constraint misslyckades på databasnivå"));

        // 2 & 3. Act & Assert
        // Vi verifierar att felet bubblar upp hela vägen ut ur vår UserService
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(reqDto);
        }, "Borde kasta ett fel om databasen misslyckas med att spara");

        assertEquals("Databasen är offline eller unik constraint misslyckades på databasnivå", exception.getMessage());

        // SÄKERHETSKONTROLL: Eftersom save() smällde, får mappen ALDRIG anropas efteråt!
        verify(userMapper, never()).userToDto(any(User.class));
    }

    @Test
    public void loadUserByUsername_Success() {
        // 1. Arrange (Testspecifik setup)
        String username = "Handsome_Henry";

        // Säg till databas-mocken att returnera vår testUser när man söker efter namnet
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // 2. Act - Kör metoden från UserDetailsService gränssnittet
        UserDetails actualDetails = userService.loadUserByUsername(username);

        // 3. Assert - Kontrollera att detaljerna som returneras matchar vår användare
        assertNotNull(actualDetails, "UserDetails borde inte vara null");
        assertEquals(testUser.getUsername(), actualDetails.getUsername());
        assertEquals(testUser.getPassword(), actualDetails.getPassword());
    }

    @Test
    public void loadUserByUsername_ThrowsException_WhenUserNotFound() {
        // 1. Arrange (Testspecifik setup)
        String username = "Unknown_User";

        // Säg till databas-mocken att användaren inte kan hittas i systemet
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // 2 & 3. Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        }, "Borde ha kastat UsernameNotFoundException när användaren saknas");

        assertEquals("Användaren hittades inte", exception.getMessage());
    }

    @Test
    public void usernameExists_ReturnsTrue_WhenExists() {
        // 1. Arrange
        String username = "Handsome_Henry";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // 2. Act
        boolean exists = userService.usernameExists(username);

        // 3. Assert
        assertTrue(exists, "Borde returnera true om användarnamnet finns i databasen");
    }

    @Test
    public void usernameExists_ReturnsFalse_WhenDoesNotExist() {
        // 1. Arrange (Testspecifik setup)
        String username = "Missing_User";

        // Säg till databas-mocken att användarnamnet INTE finns (tom Optional)
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // 2. Act - Kör metoden vi testar
        boolean exists = userService.usernameExists(username);

        // 3. Assert - Kontrollera att den faktiskt svarar false
        assertFalse(exists, "Borde returnera false om användarnamnet är ledigt");
    }

    @Test
    public void emailExists_ReturnsTrue_WhenExists() {
        // 1. Arrange (Testspecifik setup)
        String email = "henry@veryhandsome.com";

        // Säg till databas-mocken att e-postadressen redan FINNS
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // 2. Act - Kör metoden vi testar
        boolean exists = userService.emailExists(email);

        // 3. Assert - Kontrollera att den faktiskt svarar true
        assertTrue(exists, "Borde returnera true om e-postadressen redan är registrerad");
    }

    @Test
    public void emailExists_ReturnsFalse_WhenDoesNotExist() {
        // 1. Arrange
        String email = "missing@domain.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // 2. Act
        boolean exists = userService.emailExists(email);

        // 3. Assert
        assertFalse(exists, "Borde returnera false om e-postadressen är ledig");
    }
}
