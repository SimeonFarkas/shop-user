package se.jensen.simeon.shopuser.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.jensen.simeon.shopuser.model.User;
import se.jensen.simeon.shopuser.repository.UserRepository;

import java.io.IOException;

/**
 * Filter som körs automatiskt på varje inkommande HTTP-request.
 *
 * <p>Filtret kontrollerar om requesten innehåller en giltig JWT-token i
 * Authorization-headern. Om token är giltig hämtas användaren från databasen
 * och Spring Security informeras om vem användaren är, så att den kan
 * kontrollera behörigheter för den aktuella requesten.</p>
 *
 * <p>Flödet för varje request är:</p>
 * <ol>
 *     <li>Filtret letar efter Authorization-headern i requesten</li>
 *     <li>Om headern saknas eller inte börjar med "Bearer " släpps requesten igenom
 *         och Spring Security hanterar den som en oinloggad användare</li>
 *     <li>Om headern finns extraheras token-strängen</li>
 *     <li>Token valideras med JwtUtil</li>
 *     <li>Om token är giltig hämtas användarens ID ur token</li>
 *     <li>Användaren hämtas från databasen med ID:t</li>
 *     <li>Spring Security informeras om att användaren är inloggad</li>
 *     <li>Requesten släpps igenom till rätt controller</li>
 * </ol>
 * author Simeon
 * Dokumenterad: 2026-06-01
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Hämta Authorization-headern från requesten
        // Exempel på hur headern ser ut: "Bearer eyJhbGciOiJSUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // Om headern saknas eller inte börjar med "Bearer " finns ingen token
        // Släpp igenom requesten, Spring Security hanterar den som oinloggad
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ta bort "Bearer " från strängen för att få själva token
        // "Bearer eyJhbGciOiJSUzI1NiJ9..." -> "eyJhbGciOiJSUzI1NiJ9..."
        String token = authHeader.substring(7);

        // Kontrollera om token är giltig innan vi försöker läsa ur den
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Hämta användarens ID ur token
        String userId = jwtUtil.extractUserId(token);

        // Hämta användaren från databasen med ID:t
        User user = userRepository.findById(Long.valueOf(userId))
                .orElse(null);

        // Om användaren inte finns i databasen, släpp igenom som oinloggad
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skapa ett autentiseringsobjekt som Spring Security förstår
        // Detta talar om för Spring Security vem användaren är och vilka
        // rättigheter de har, baserat på getAuthorities() i User-klassen
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user,
                        null,
                        user.getAuthorities()
                );

        // Koppla autentiseringen till den aktuella requesten
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Släpp igenom requesten till rätt controller
        filterChain.doFilter(request, response);
    }
}
