package com.pm.catalogservice.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class CustomTokenService {

    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    /**
     * Verify a JWT access token.
     *
     * @param token the JWT access token to verify
     * @return true if the token is valid and not expired, false otherwise
     * @throws JOSEException  if there is an error during token verification
     * @throws ParseException if the token cannot be parsed
     */
    public boolean verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verifiedJWT = signedJWT.verify(verifier);

        return verifiedJWT && expirationTime.after(new Date());
    }

    /**
     * Extract the email (subject) from a valid JWT access token.
     *
     * @param token a valid JWT access token
     * @return the email of the user
     * @throws ParseException  if the token cannot be parsed
     * @throws JOSEException   if the token cannot be verified
     * @throws RuntimeException if the token is invalid or expired
     */
    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        if (!verifyToken(token)) {
            throw new RuntimeException("Token is not valid");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }
}
