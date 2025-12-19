package com.ares.ares_server.service;

import com.ares.ares_server.domain.User;
import com.ares.ares_server.dto.AuthDTO;
import com.ares.ares_server.dto.CredentialsDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.exceptios.InvalidCredentialsException;
import com.ares.ares_server.exceptios.UserAlreadyExistsException;
import com.ares.ares_server.exceptios.UserDoesNotExistsException;
import com.ares.ares_server.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public AuthDTO signUp(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists!");
        }

        User user = userMapper.fromDto(userDTO);
        User savedUser = userRepository.save(user);

        String token = generateToken(savedUser.getEmail());

        return new AuthDTO(token);
    }

    public String login(CredentialsDTO loginCredentials) {
        Optional<User> user = userRepository.findByEmail(loginCredentials.getEmail());
        if (!(user.isPresent() && user.get().getEncryptedPassword().equals(loginCredentials.getPassword())))
            throw new InvalidCredentialsException("Invalid email or password");
        return generateToken(loginCredentials.getEmail());

    }

    public void changePassword(String email, String newPassword) {
        if (! userRepository.existsByEmail(email))
            throw new UserDoesNotExistsException("User with email " + email + " does not exist!");
        User user = userRepository.findByEmail(email).get();
        user.setEncryptedPassword(newPassword);
        userRepository.save(user);
    }

    public String refresh(CredentialsDTO refreshCredentials) {
        String token = login(refreshCredentials);
        if (!validateToken(token)) {
            throw new InvalidCredentialsException("Invalid credentials for token refresh");
        }
        return token;
    }

    // Add these methods to your AuthService class
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // This returns the email we set in generateToken
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractUsername(token);
            return tokenEmail != null && tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}