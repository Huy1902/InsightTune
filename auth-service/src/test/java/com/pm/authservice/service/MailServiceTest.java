package com.pm.authservice.service;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendMail_success() throws Exception {
        // given
        String email = "test@example.com";
        String otp = "123456";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Set giá trị cho appEmail
        ReflectionTestUtils.setField(mailService, "appEmail", "no-reply@example.com");

        // fake template
        InputStream fakeTemplate =
                new java.io.ByteArrayInputStream("OTP = [[OTP_CODE]]".getBytes(StandardCharsets.UTF_8));

        // spy để override getTemplateStream()
        MailService spyService = spy(mailService);
        doReturn(fakeTemplate).when(spyService).getTemplateStream();

        // when
        spyService.sendMail(email, otp);

        // then
        verify(mailSender, times(1)).send(mimeMessage);
    }

}
