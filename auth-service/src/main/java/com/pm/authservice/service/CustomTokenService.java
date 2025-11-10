package com.pm.authservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.UserRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Service handling JWT and refresh token operations.
 * <p>
 * Includes the following functionalities:
 * <ul>
 *     <li>Generate access token</li>
 *     <li>Generate refresh token</li>
 *     <li>Refresh access token using a refresh token</li>
 *     <li>Verify token and extract email from token</li>
 * </ul>
 * </p>
 */
@Service
@Slf4j
public class CustomTokenService {
    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_VALIDITY = 30L * 24 * 60 * 60 * 1000; // 30 ngày

    public CustomTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    /**
     * Build scope for a user based on their role.
     *
     * @param user the user for whom to create the scope
     * @return a space-separated scope string
     */
    private String buildScope(User user) {
        StringJoiner scope = new StringJoiner(" ");
        if (user.getRole() != null) {
            scope.add(user.getRole().getName());
        }

        return scope.toString();
    }


    /**
     * Generate an access token for a user.
     *
     * @param user the user for whom to create the token
     * @return a JWT access token
     */
    public String generateAccessToken(User user) {

        if (user.getEmail() == null) {
            throw new RuntimeException("Cant generate access token because email is null");
        }

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("auth-service")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(15, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a refresh token for a user and save it in the database.
     *
     * @param user the user for whom to create the refresh token
     * @return the refresh token as a string
     */
    public String generateRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiry(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));

        return refreshTokenRepository.save(token).getToken();
    }

    /**
     * Generate a new access token from a refresh token.
     *
     * @param refreshTokenValue the current refresh token
     * @return AuthenticationResponse containing the new access token and user information
     * @throws AppException if the refresh token is invalid or expired
     */
    @PostAuthorize("returnObject.email == authentication.name")
    public AuthenticationResponse refreshAccessToken(String refreshTokenValue) {
        // check if refreshToken is overdue
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESHTOKEN_INVALID));

        if (refreshToken.getExpiry().before(new Date())) {
            throw new AppException(ErrorCode.REFRESHTOKEN_ISREVOKED);
        }

        // find user
        User user = userRepository.findById(refreshToken.getUser().getId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOTFOUND)
        );
        String newAccessToken = generateAccessToken(user);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .email(user.getEmail())
                .authenticated(true)
                .build();
    }

    /**
     * Verify a JWT access token.
     *
     * @param token the access token to verify
     * @return true if the token is valid and not expired, false otherwise
     * @throws JOSEException  if an error occurs during verification
     * @throws ParseException if the token is invalid
     */
    public boolean verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verifiedJWT = signedJWT.verify(verifier);

        return verifiedJWT && expirationTime.after(new Date());
    }

    /**
     * Retrieve the email (subject) from a JWT access token.
     *
     * @param token a valid access token
     * @return the user's email
     * @throws ParseException  if the token is invalid
     * @throws JOSEException   if the token is invalid
     * @throws RuntimeException if the token is invalid or has expired
     */
    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        if (!verifyToken(token)) {
            throw new RuntimeException("Token is not valid");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);

        // get claim 'sub' (email)
        return signedJWT.getJWTClaimsSet().getSubject();
    }

}
