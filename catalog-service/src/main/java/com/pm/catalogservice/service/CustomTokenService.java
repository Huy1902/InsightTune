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
     * Xác thực token JWT.
     *
     * @param token access token cần verify
     * @return true nếu token hợp lệ và chưa hết hạn, false nếu không hợp lệ
     * @throws JOSEException  nếu có lỗi khi verify
     * @throws ParseException nếu token không hợp lệ
     */
    public boolean verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verifiedJWT = signedJWT.verify(verifier);

        return verifiedJWT && expirationTime.after(new Date());
    }

    /**
     * Lấy email (subject) từ access token JWT.
     *
     * @param token access token hợp lệ
     * @return email của user
     * @throws ParseException  nếu token không hợp lệ
     * @throws JOSEException   nếu token không hợp lệ
     * @throws RuntimeException nếu token không hợp lệ hoặc đã hết hạn
     */
    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        if (!verifyToken(token)) {
            throw new RuntimeException("Token is not valid");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);

        // Lấy claim 'sub' (email)
        return signedJWT.getJWTClaimsSet().getSubject();
    }
}
