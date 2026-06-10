package se.jensen.simeon.shopuser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. FÅNGA VALIDERINGSERFEL (DTO-regler)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. FÅNGA FEL INLOGGNING
    @ExceptionHandler({BadCredentialsException.class, org.springframework.security.core.userdetails.UsernameNotFoundException.class})
    public ResponseEntity<String> handleBadCredentials(Exception ex) { // Ändra typen till Exception här
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Fel användarnamn/e-post eller lösenord. Försök igen.");
    }

    // 3. FÅNGA NEKAD TILLGÅNG (Fel roll)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Du har inte behörighet att se den här resursen.");
    }

    // 4. FÅNGA AFFÄRSLOGIKFEL (T.ex. upptaget användarnamn från din Service)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        // Vi använder 409 Conflict eftersom datan krockar med befintlig data
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // 5. FÅNGA SAKER SOM INTE HITTAS (Om du skapar en egen ResourceNotFoundException)
    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    // }

    // 6. LIVLINAN (Fångar upp alla buggar och serverkrascher automatiskt)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // Logga gärna felet i din server-konsol så att du ser vad som hände under utveckling
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ett oväntat fel inträffade på servern. Försök igen senare.");
    }
}
