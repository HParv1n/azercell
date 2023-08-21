package az.azercell.customer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "2fbb5d7c0c13e52e4d6ab7f9021f98d63ec32a0d5cf63e58";
    private static final long EXPIRATION_TIME = 3600000; // 1 saat

    public String generateToken(String phoneNumber) {
        Claims claims = Jwts.claims().setSubject(phoneNumber);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}
