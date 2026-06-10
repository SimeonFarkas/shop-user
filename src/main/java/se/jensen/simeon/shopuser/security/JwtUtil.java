package se.jensen.simeon.shopuser.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.jensen.simeon.shopuser.model.User;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Verktyg för att hantera JWT-tokens med asymmetrisk RSA-kryptering.
 *
 * <p>Asymmetrisk kryptering innebär att vi använder två olika nycklar:</p>
 * <ul>
 *     <li>Den privata nyckeln används för att SIGNERA tokens när en användare loggar in.
 *         Denna nyckel ska aldrig delas med någon och förvaras säkert i miljövariabler.</li>
 *     <li>Den publika nyckeln används för att VERIFIERA att en token är äkta när en
 *         användare skickar en request. Denna nyckel kan delas med andra tjänster,
 *         t.ex. ProductFeed, så att de också kan verifiera tokens.</li>
 * </ul>
 *
 * <p>Flödet är:</p>
 * <ol>
 *     <li>Användaren loggar in med användarnamn och lösenord</li>
 *     <li>AuthController anropar generateToken() som skapar en signerad token med användarens ID</li>
 *     <li>Token skickas tillbaka till frontend och sparas där</li>
 *     <li>Vid varje efterföljande request skickar frontend med token i Authorization-headern</li>
 *     <li>JwtFilter anropar isTokenValid() för att kontrollera att token är äkta och inte utgången</li>
 *     <li>JwtFilter anropar extractUserId() för att ta reda på vem användaren är</li>
 * </ol>
 *
 * @author Simeon
 * Dokumenterad: 2026-05-25
 */
@Component
public class JwtUtil {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtUtil(
            @Value("${jwt.private-key}") String privateKeyStr,
            @Value("${jwt.public-key}") String publicKeyStr
    ) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] privateBytes = Base64.getDecoder().decode(privateKeyStr.replaceAll("\\s", ""));
        this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

        byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr.replaceAll("\\s", ""));
        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(privateKey)
                .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
