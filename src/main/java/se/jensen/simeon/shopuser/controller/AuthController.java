package se.jensen.simeon.shopuser.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.jensen.simeon.shopuser.dto.*;
import se.jensen.simeon.shopuser.mapper.UserMapper;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.security.JwtUtil;
import se.jensen.simeon.shopuser.service.UserService;

/**
 * AuthController hanterar klientens "requests" till /auth.
 * Klassen är ett REST API-lager och agerar som en mellanhand mellan klienten och affärslogiken (TokenService).
 * <p>
 * Klassen ansvarar för inloggning, utloggning, registrering och kontroller
 * om användarnamn/email redan är upptaget.
 * <p>
 * Den annoteras med RestController för att indikera till Spring att den ska
 * hantera HTTP-requests och returnera JSON till klienten.
 *
 * @author Simeon
 * Dokumenterad: 2026-06-02
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = new UserMapper();
    }

    /**
     * Registrerar en ny användare i systemet.
     * <p>
     * Metoden tar emot användaruppgifter, validerar dem enligt restriktionerna i DTO-klassen,
     * skickar datan vidare till affärslogiken för kryptering och lagring, samt returnerar
     * den skapade resursen.
     * </p>
     *
     * @param userReqDto Data Transfer Object (DTO) som innehåller registreringsdata från klienten.
     *                   {@code @Valid} säkerställer att alla fältvalideringar (t.ex. {@code @Email},
     *                   {@code @NotBlank}) uppfylls innan metoden exekveras.
     *                   {@code @RequestBody} mappar inkommande JSON-data till Java-objektet.
     * @return En {@link ResponseEntity} innehållande en {@link UserRespDto} tillsammans med
     * HTTP-status 201 (CREATED).
     */
    @PostMapping("/register")
    public ResponseEntity<UserRespDto> register(@Valid @RequestBody UserReqDto userReqDto) {
        UserRespDto userRespDto = userService.registerUser(userReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRespDto);
    }

    /**
     * Logga in på befintlig profil.
     *
     * @param loginRequest Användaren skriver in antingen alias eller email.
     * @return HTTP 200 (ok) och en LoginResponseDTO.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginRespDto> login(@Valid @RequestBody LoginReqDto loginRequest) {
        // 1. Vi tar användarnamnet och lösenordet som fyllts i på skärmen (loginRequest).
        // 2. authenticationManager letar upp rätt rad i databasen baserat på namnet.
        // 3. Den kontrollerar om lösenordet stämmer, och sparar sedan hela användaren i 'auth'-objektet.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.usernameOrEmail(), loginRequest.password())
        );

        // 4. authenticationManager har använt infon från loginRequest för att hämta rätt rad från databasen.
        // 5. auth.getPrincipal() hämtar den specifika användaren (t.ex. Kalle) som lyckades logga in.
        // 6. (User) talar om för Java att detta är vår egen User-klass så att vi kan spara den i 'loggedInUser'.
        User loggedInUser = (User) auth.getPrincipal();

        // 7. Nu kan vi skicka vår färdiga användare till jwtUtil för att skapa inloggnings-nyckeln (token).
        String jwt = jwtUtil.generateToken(loggedInUser);

        // 8. Vi använder din userMapper direkt för att göra om vår User till en UserRespDto.
        UserRespDto userResponse = userMapper.userToDto(loggedInUser);

        // 9. Returnera token och användarinfo till frontenden
        return ResponseEntity.ok(new LoginRespDto(jwt, userResponse));
    }

    /**
     * Loggar ut användaren.
     * Eftersom JWT-tokens är stateless (servern sparar ingen session) hanteras
     * själva utloggningen i frontend genom att ta bort token från localStorage.
     *
     * @return HTTP 204 (no content) som bekräftar att utloggningen lyckades.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /**
     * En boolean för att kontrollera om alias redan finns.
     *
     * @param username Användarens alias.
     * @return HTTP 200 (ok) och en AvailabilityResponseDTO.
     */
    @GetMapping("/check-username")
    public ResponseEntity<AvailabilityRespDto> checkUsernameAvailable(@RequestParam String username) {
        // Vi frågar userService om namnet är upptaget
        boolean exists = userService.usernameExists(username);

        // Returnerar true om det är ledigt (!exists), false om det är upptaget
        return ResponseEntity.ok(new AvailabilityRespDto(!exists));
    }

    /**
     * En boolean för att kontrollera om email redan finns.
     *
     * @param email Användarens email.
     * @return HTTP 200 (ok) och en AvailabilityResponseDTO.
     */
    @GetMapping("/check-email")
    public ResponseEntity<AvailabilityRespDto> checkEmailAvailable(@RequestParam String email) {
        // Vi frågar userService om e-posten är upptagen
        boolean exists = userService.emailExists(email);

        // Returnerar true om det är ledigt (!exists), false om det är upptaget
        return ResponseEntity.ok(new AvailabilityRespDto(!exists));
    }
}
