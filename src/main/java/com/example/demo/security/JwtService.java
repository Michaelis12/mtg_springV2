package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.example.demo.security.ConfigurePasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entities.DeckCreator;
import com.example.demo.repositories.DeckBuilderRepository;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
	
	@Autowired
	private DeckBuilderRepository deckBuilderRepository;
	
	
	@Autowired
	private ConfigurePasswordEncoder passwordEncoder;
	
	// algorithme de signature utilisé pour signer et vérifier le JWT
    final static private SignatureAlgorithm alg = SignatureAlgorithm.HS256;
    
    private final static SecretKey SECRET_KEY = generateSecretKey();
    
    // génère une clé secrète en utilisant l'algorithme HS256
    
    private static SecretKey generateSecretKey() {
    	
        return Keys.secretKeyFor(alg);
    }
   
    
    // récupère dans le token les paramètres de userDetails (username, roles)
    public String generateToken (UserDetails userDetails){
        return generateToken(userDetails, new HashMap<>());
    }

    public String generateToken (UserDetails userDetails, Map<String, String> extraClaims){
        return Jwts
                .builder()
                // permet de récupérer les informations payload du token et de les extraire
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(alg, SECRET_KEY)
                .compact();
    }
    
    /*
    public String generatePasswordResetToken(String email) {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);  // Ajouter l'email en claim
        claims.put("type", "password-reset");  // Indicateur pour différencier les tokens

        // Token qui expire après 1 heure
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)  // Utilisation de l'email comme subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // Expiration dans 1 heure
                .signWith(alg, SECRET_KEY)
                .compact();
    }
    */

    // conditions de validité du token 
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    /*
    // Vérifie si un token de réinitialisation est valide
    public boolean isPasswordResetTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return "password-reset".equals(tokenType) && !isTokenExpired(token);  // Vérifie le type et l'expiration
        } catch (Exception e) {
            return false;
        }
    }
    */
    
    // extrait le nom d'user du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // compare la date d'expiration extraite avec la date actuelle
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
 // extrait la date d'expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);

    }
    
    // extraction de toutes les informations
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    /*
    public String resetPassword(String token, String newPassword) {
	    // Valider le token de réinitialisation
	    if (isPasswordResetTokenValid(token)) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de réinitialisation invalide ou expiré");
	    }

	    // Extraire l'email du token
	    Claims claims = extractAllClaims(token);
	    String email = claims.get("email", String.class);

	    // Récupérer l'utilisateur à partir de l'email
	    DeckCreator user = deckBuilderRepository.findByEmail(email)
	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

	    // Mettre à jour le mot de passe de l'utilisateur
	    user.setPassword(passwordEncoder.encode(newPassword));  // Encodage du nouveau mot de passe

	    // Sauvegarder l'utilisateur avec le nouveau mot de passe
	    deckBuilderRepository.save(user);

	    return "Mot de passe réinitialisé avec succès.";
	}
    */

}