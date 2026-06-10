package se.jensen.simeon.shopuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import se.jensen.simeon.shopuser.controller.AuthController;
import se.jensen.simeon.shopuser.dto.LoginReqDto;
import se.jensen.simeon.shopuser.dto.UserReqDto;
import se.jensen.simeon.shopuser.dto.UserRespDto;
import se.jensen.simeon.shopuser.exception.GlobalExceptionHandler;
import se.jensen.simeon.shopuser.mapper.UserMapper;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.security.JwtUtil;
import se.jensen.simeon.shopuser.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // Ren Mockito - ingen Spring-kontext startas!
public class AuthControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserMapper userMapper; // Nu mockar vi denna korrekt!

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;

    @BeforeEach
    void setUp() {
        // Vi bygger kontrollern helt manuellt genom att skicka in våra @Mocks i konstruktorn.
        // Detta är den renaste formen av Dependency Injection i ett enhetstest!
        AuthController authController = new AuthController(userService, authenticationManager, jwtUtil, userMapper);

        // Vi skapar en lokal validator och registrerar den i MockMvc
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet(); // Initierar validatorn

        // Skapa MockMvc baserat på vår helt isolerade kontroller
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setValidator(validator) // <--- DETTA AKTIVERAR @Valid!
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Testdata
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Handsome_Henry");
        testUser.setEmail("henry@veryhandsome.com");
        testUser.setPassword("hashed_password");
        testUser.setRole("USER");
    }

    // ==========================================
    // TESTER FÖR /auth/register
    // ==========================================

    @Test
    public void testRegister_Success() throws Exception {
        // 1. Arrange (Testspecifik setup)
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsome.com", "password");
        UserRespDto expectedResp = new UserRespDto(1L, "Handsome_Henry", "henry@veryhandsome.com", "USER");

        // När kontrollern anropar servicen, returnera vår förväntade DTO
        when(userService.registerUser(any(UserReqDto.class))).thenReturn(expectedResp);

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))) // Gör om DTO till JSON-text
                .andExpect(status().isCreated()) // Kontrollera HTTP 201 Created
                .andExpect(jsonPath("$.id").value(1L)) // Kontrollera JSON-datan som kommer tillbaka
                .andExpect(jsonPath("$.username").value("Handsome_Henry"))
                .andExpect(jsonPath("$.email").value("henry@veryhandsome.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    // ==========================================
    // TESTER FÖR /auth/login
    // ==========================================

    @Test
    public void testLogin_Success() throws Exception {
        // 1. Arrange (Testspecifik setup)
        LoginReqDto loginReq = new LoginReqDto("Handsome_Henry", "password");

        // Vi skapar den förväntade DTO:n som mappen ska spotta ur sig
        UserRespDto expectedUserResp = new UserRespDto(1L, "Handsome_Henry", "henry@veryhandsome.com", "USER");
        String dummyJwt = "mocked-jwt-token";

        // Mocka Spring Securitys inloggningskedja
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // Mocka token-genereringen
        when(jwtUtil.generateToken(testUser)).thenReturn(dummyJwt);

        // HÄR KNYTER VI IHOP DET: Säg till din mockade mapper att faktiskt returnera DTO:n!
        when(userMapper.userToDto(testUser)).thenReturn(expectedUserResp);

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(dummyJwt)) // Verifiera token
                .andExpect(jsonPath("$.user.id").value(1L)) // Verifiera användardata hela vägen ner!
                .andExpect(jsonPath("$.user.username").value("Handsome_Henry"))
                .andExpect(jsonPath("$.user.email").value("henry@veryhandsome.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        // SÄKERHETSKONTROLL: Verifiera att kontrollern faktiskt skickade in rätt användare till mappen
        verify(userMapper, times(1)).userToDto(testUser);
    }

    // ==========================================
    // TESTER FÖR /auth/logout
    // ==========================================

    @Test
    public void testLogout_Success() throws Exception {
        // 1. Arrange - Krävs inga förberedelser då metoden är stateless och tom

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent()); // Kontrollera HTTP 204 No Content
    }

    // ==========================================
    // TESTER FÖR /auth/check-username
    // ==========================================

    @Test
    public void testCheckUsernameAvailable_ReturnsTrue_WhenUsernameIsFree() throws Exception {
        // 1. Arrange
        String username = "New_User";
        // Om namnet inte finns, svarar servicen false på frågan "exists"
        when(userService.usernameExists(username)).thenReturn(false);

        // 2 & 3. Act & Assert
        mockMvc.perform(get("/auth/check-username").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true)); // Tillgänglig blir true (!exists)
    }

    @Test
    public void testCheckUsernameAvailable_ReturnsFalse_WhenUsernameIsTaken() throws Exception {
        // 1. Arrange
        String username = "Handsome_Henry";
        // Om namnet finns, svarar servicen true på frågan "exists"
        when(userService.usernameExists(username)).thenReturn(true);

        // 2 & 3. Act & Assert
        mockMvc.perform(get("/auth/check-username").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false)); // Tillgänglig blir false (!exists)
    }

    // ==========================================
    // TESTER FÖR /auth/check-email
    // ==========================================

    @Test
    public void testCheckEmailAvailable_ReturnsTrue_WhenEmailIsFree() throws Exception {
        // 1. Arrange
        String email = "free@domain.com";
        when(userService.emailExists(email)).thenReturn(false);

        // 2 & 3. Act & Assert
        mockMvc.perform(get("/auth/check-email").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    public void testCheckEmailAvailable_ReturnsFalse_WhenEmailIsTaken() throws Exception {
        // 1. Arrange
        String email = "henry@veryhandsome.com";
        when(userService.emailExists(email)).thenReturn(true);

        // 2 & 3. Act & Assert
        mockMvc.perform(get("/auth/check-email").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    // ==========================================
    // NEGATIVA VALIDERINGSTESTER
    // ==========================================

    @Test
    public void testRegister_ReturnsBadRequest_WhenUsernameOrEmailIsInvalid() throws Exception {
        // 1. Arrange - Vi skapar en trasig DTO (t.ex. tomt användarnamn och felaktig mailadress)
        // (Här antar vi att din UserReqDto har validering som @NotBlank och @Email)
        UserReqDto invalidReqDto = new UserReqDto("", "inte-en-riktig-mail.com", "123");

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReqDto)))
                .andExpect(status().isBadRequest()); // Kontrollera HTTP 400 Bad Request!

        // SÄKERHETSKONTROLL: Servicen får ALDRIG anropas om valideringen misslyckas!
        verify(userService, never()).registerUser(any(UserReqDto.class));
    }

    @Test
    public void testRegister_ReturnsBadRequest_WhenUsernameAlreadyExists() throws Exception {
        // 1. Arrange
        UserReqDto reqDto = new UserReqDto("Handsome_Henry", "henry@veryhandsome.com", "password");

        // Säg till servicen att kasta ett IllegalArgumentException (precis som den gör i verkligheten)
        when(userService.registerUser(any(UserReqDto.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isConflict()); // <--- Vi vill att detta ska bli 409!
    }

    @Test
    public void testLogin_ReturnsBadRequest_WhenFieldsAreBlank() throws Exception {
        // 1. Arrange - En inloggning med tomma fält
        LoginReqDto invalidLoginReq = new LoginReqDto("", "");

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginReq)))
                .andExpect(status().isBadRequest()); // Kontrollera HTTP 400 Bad Request!

        // SÄKERHETSKONTROLL: Security-managern får aldrig försöka autentisera tomma fält!
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    public void testLogin_ReturnsUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // 1. Arrange - Vi skickar in ett försök till inloggning
        LoginReqDto loginReq = new LoginReqDto("Handsome_Henry", "wrong_password");

        // Vi säger till Spring Securitys manager att kasta ett BadCredentialsException (fel lösenord/användare)
        // Du kan behöva importera: org.springframework.security.authentication.BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Fel användarnamn eller lösenord"));

        // 2 & 3. Act & Assert
        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist()); // <--- SÄKERSTÄLLER ATT INGEN TOKEN FINNS MED!

        // SÄKERHETSKONTROLL: Eftersom inloggningen misslyckades, får vi ALDRIG generera en token eller mappa användaren!
        verify(jwtUtil, never()).generateToken(any());
        verify(userMapper, never()).userToDto(any());
    }

    @Test
    public void testLogin_ReturnsUnauthorized_WhenUserDoesNotExist() throws Exception {
        // 1. Arrange - Vi försöker logga in med en användare som inte finns
        LoginReqDto loginReq = new LoginReqDto("Ghost_User", "some_password");

        // Vi simulerar att vår authenticationManager kastar UsernameNotFoundException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // 2 & 3. Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized()) // <--- Ska fortfarande ge 401 Unauthorized!
                .andExpect(jsonPath("$.token").doesNotExist()); // <--- Ingen token får läcka ut

        // SÄKERHETSKONTROLL: Inga interna logik-komponenter får köras vidare
        verify(jwtUtil, never()).generateToken(any());
        verify(userMapper, never()).userToDto(any());
    }
}
