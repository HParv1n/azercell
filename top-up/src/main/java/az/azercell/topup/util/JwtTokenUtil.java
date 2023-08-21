package az.azercell.topup.util;

import az.azercell.topup.exceptions.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

    private static final String secret = "2fbb5d7c0c13e52e4d6ab7f9021f98d63ec32a0d5cf63e58";

    public static String extractPhoneNumber(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static void validateToken(String jwtToken) {
        if (isTokenExpired(jwtToken)) {
            throw new TokenExpiredException("Token has expired.");
        }
    }

    public static boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate != null && expirationDate.before(new Date());
    }

    private static Date extractExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}