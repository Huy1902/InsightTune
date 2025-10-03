package com.pm.authservice.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pm.authservice.dto.response.AuthenticationResponse;
import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import com.pm.authservice.models.RefreshToken;
import com.pm.authservice.models.Role;
import com.pm.authservice.models.User;
import com.pm.authservice.repository.RefreshTokenRepository;
import com.pm.authservice.repository.UserRepository;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomTokenServiceTest {

    @InjectMocks
    private CustomTokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    // Mock SIGNER_KEY
    @BeforeEach
    void setup() throws Exception {
        ReflectionTestUtils.setField(tokenService, "SIGNER_KEY", "0123456789abcdef0123456789abcdef");
    }

    // successful generate accessToken
    @Test
    void generateAccessToken_thenContainUserEmail() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        String token = tokenService.generateAccessToken(user);

        assertNotNull(token);

        // verify decode token
        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals("test@example.com", signedJWT.getJWTClaimsSet().getSubject());
    }

    @Test
    void whenUserEmailIsNull_generateAccessToken_thenThrowException() {
        User user = new User();
        user.setEmail(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.generateAccessToken(user));

        assertEquals("Cant generate access token because email is null", ex.getMessage());
    }

    // refreshToken
    @Test
    void generateRefreshToken_thenReturnTokenFromRepository() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        RefreshToken savedToken = new RefreshToken();
        savedToken.setToken("refresh-token-123");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        String token = tokenService.generateRefreshToken(user);

        assertEquals("refresh-token-123", token);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void whenUserIdIsNull_generateRefreshToken_thenThrowException() {
        User user = new User(); // không set ID

        assertThrows(NullPointerException.class, () -> tokenService.generateRefreshToken(user));
    }

    // refresh accessToken
    @Test
    void whenValidToken_refreshAccessToken_thenReturnNewAccessToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiry(new Date(System.currentTimeMillis() + 10000)); // còn hạn

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AuthenticationResponse response = tokenService.refreshAccessToken("refresh-token");

        assertNotNull(response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isAuthenticated());
    }

    @Test
    void whenTokenExpired_refreshAccessToken_thenThrowException() {
        User user = new User();
        user.setId(1L);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiry(new Date(System.currentTimeMillis() - 1000)); // hết hạn

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));

        AppException ex = assertThrows(AppException.class,
                () -> tokenService.refreshAccessToken("refresh-token"));

        assertEquals(ErrorCode.REFRESHTOKEN_ISREVOKED, ex.getError());
    }

    @Test
    void whenTokenNotFound_refreshAccessToken_thenThrowException() {
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tokenService.refreshAccessToken("invalid-token"));

        assertEquals(ErrorCode.REFRESHTOKEN_INVALID, ex.getError());
    }

    @Test
    void whenUserNotFound_refreshAccessToken_thenThrowException() {
        User user = new User();
        user.setId(1L);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiry(new Date(System.currentTimeMillis() + 10000));

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tokenService.refreshAccessToken("refresh-token"));

        assertEquals(ErrorCode.USER_NOTFOUND, ex.getError());
    }

    // verifyToken
    @Test
    void whenValid_verifyToken_thenReturnTrue() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        String token = tokenService.generateAccessToken(user);

        boolean valid = tokenService.verifyToken(token);

        assertTrue(valid);
    }

    @Test
    void whenTokenExpired_verifyToken_thenReturnFalse() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");

        // Tạo token với thời gian hết hạn đã qua
        Date now = new Date();
        Date expired = new Date(now.getTime() - 1000); // hết hạn 1 giây trước

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(now)
                .expirationTime(expired)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJWT.sign(new MACSigner("Q+W+8at86K9vMPRa3ZZEHWOL6wiQzDmNS0X9Wu8omL2h5qgfIIb0oEchMm+v49MP")); // thay bằng key thật

        String token = signedJWT.serialize();

        boolean valid = tokenService.verifyToken(token);

        assertFalse(valid);
    }

    // get email from token
    @Test
    void whenValid_getEmailFromToken_thenReturnEmail() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        String token = tokenService.generateAccessToken(user);

        String email = tokenService.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void whenTokenMalformed_getEmailFromToken_thenThrowParseException() {
        String invalidToken = "this-is-not-a-jwt";

        assertThrows(ParseException.class, () -> tokenService.getEmailFromToken(invalidToken));
    }

    @Test
    void whenTokenSignatureInvalid_getEmailFromToken_thenThrowRuntimeException() throws Exception {
        // Tạo token giả không ký đúng
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test@example.com")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJWT.sign(new MACSigner("01234567890123456789012345678901")); // sai key

        String token = signedJWT.serialize();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tokenService.getEmailFromToken(token));

        assertEquals("Token is not valid", ex.getMessage());
    }
}
