package com.pm.catalogservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CustomTokenServiceTest {

    private CustomTokenService customTokenService;
    private String signerKey;

    @BeforeEach
    void setUp() {
        customTokenService = new CustomTokenService();
        signerKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";
        ReflectionTestUtils.setField(customTokenService, "SIGNER_KEY", signerKey);
    }

    private String generateToken(String email, Instant expirationTime) throws JOSEException {
        JWSSigner signer = new MACSigner(signerKey.getBytes());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .expirationTime(Date.from(expirationTime))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    @Test
    void verifyToken_withValidToken_shouldReturnTrue() throws Exception {
        String token = generateToken("user@example.com", Instant.now().plusSeconds(3600));

        boolean result = customTokenService.verifyToken(token);

        assertTrue(result);
    }

    @Test
    void verifyToken_withExpiredToken_shouldReturnFalse() throws Exception {
        String token = generateToken("user@example.com", Instant.now().minusSeconds(3600));

        boolean result = customTokenService.verifyToken(token);

        assertFalse(result);
    }

    @Test
    void verifyToken_withInvalidSignature_shouldReturnFalse() throws Exception {
        // Tạo token bằng key khác
        JWSSigner otherSigner = new MACSigner("anotherwrongsecretkey1234567890abcd".getBytes());
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user@example.com")
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(otherSigner);
        String badToken = signedJWT.serialize();

        assertFalse(customTokenService.verifyToken(badToken));
    }

    @Test
    void getEmailFromToken_withValidToken_shouldReturnEmail() throws Exception {
        String token = generateToken("test@mail.com", Instant.now().plusSeconds(3600));

        String email = customTokenService.getEmailFromToken(token);

        assertEquals("test@mail.com", email);
    }

    @Test
    void getEmailFromToken_withExpiredToken_shouldThrowRuntimeException() throws Exception {
        String token = generateToken("test@mail.com", Instant.now().minusSeconds(3600));

        assertThrows(RuntimeException.class, () -> customTokenService.getEmailFromToken(token));
    }

    @Test
    void getEmailFromToken_withInvalidSignature_shouldThrowRuntimeException() throws Exception {
        JWSSigner wrongSigner = new MACSigner("anotherwrongsecretkey1234567890abcd".getBytes());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("fake@mail.com")
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(wrongSigner);
        String badToken = signedJWT.serialize();

        assertThrows(RuntimeException.class, () -> customTokenService.getEmailFromToken(badToken));
    }
}
