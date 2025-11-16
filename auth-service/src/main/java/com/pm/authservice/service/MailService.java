package com.pm.authservice.service;

import com.pm.authservice.exception.AppException;
import com.pm.authservice.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * {@code MailService} is responsible for sending emails in the system.
 * <p>
 * This class uses Spring Boot's {@link JavaMailSender} to create and send HTML emails.
 * It is primarily used to send OTP (One-Time Password) verification codes to users.
 *
 * <p><strong>Main functionalities:</strong></p>
 * <ul>
 *     <li>Read HTML template files from the <code>resources/templates</code> folder.</li>
 *     <li>Replace the placeholder <code>[[OTP_CODE]]</code> in the template with the actual OTP code.</li>
 *     <li>Send emails to users with the processed HTML content.</li>
 * </ul>
 *
 * <p>Note: The template must be placed in the classpath at
 * <code>src/main/resources/templates/otp-email.html</code>.</p>
 *
 * @author Dinh
 * @version 1.0
 * @see JavaMailSender
 * @see com.pm.authservice.exception.AppException
 */
@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String appEmail;

    @Autowired
    private JavaMailSender mailSender;


    /**
     * Sends an email containing the OTP to the user.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Read the HTML template file from <code>resources/templates</code>.</li>
     *     <li>Replace the <code>[[OTP_CODE]]</code> placeholder with the actual OTP code.</li>
     *     <li>Create a {@link MimeMessage} and configure the subject, recipient, and HTML content.</li>
     *     <li>Send the email via {@link JavaMailSender}.</li>
     * </ol>
     *
     * @param email the recipient's email address
     * @param otp   the OTP code to send to the user
     * @throws MessagingException if an error occurs while creating or sending the email
     * @throws IOException if the template file cannot be read or other I/O errors occur
     * @throws com.pm.authservice.exception.AppException if the email template file is not found in the classpath
     */
    public void sendMail(String email, String otp) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent;
        try (InputStream inputStream = getTemplateStream()) {
            htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new AppException(ErrorCode.CANT_FIND_PATH);
        }
        htmlContent = htmlContent.replace("[[OTP_CODE]]", otp);

        helper.setTo(email);
        helper.setSubject("Verify your email address");
        helper.setText(htmlContent, true); // true = HTML
        helper.setFrom(appEmail);

        mailSender.send(message);
    }

    protected InputStream getTemplateStream() {
        return getClass().getResourceAsStream("/templates/otp-email.html");
    }
}
