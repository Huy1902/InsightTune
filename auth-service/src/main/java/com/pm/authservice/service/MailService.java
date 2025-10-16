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
 * {@code MailService} là lớp chịu trách nhiệm gửi email trong hệ thống.
 * <p>
 * Lớp này sử dụng {@link JavaMailSender} của Spring Boot để tạo và gửi email dạng HTML.
 * Nó được sử dụng chủ yếu để gửi mã OTP (One-Time Password) xác thực đến người dùng.
 *
 * <p><strong>Chức năng chính:</strong></p>
 * <ul>
 *     <li>Đọc file HTML template từ thư mục <code>resources/templates</code>.</li>
 *     <li>Thay thế placeholder <code>[[OTP_CODE]]</code> trong template bằng mã OTP thực tế.</li>
 *     <li>Gửi email đến người dùng với nội dung HTML đã được xử lý.</li>
 * </ul>
 *
 * <p>Lưu ý: Template phải được đặt trong classpath tại
 * <code>src/main/resources/templates/otp-email.html</code>.</p>
 *
 * @author Trần Quang Đỉnh
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
     * Gửi email chứa mã OTP đến người dùng.
     *
     * <p>Phương thức này thực hiện các bước sau:</p>
     * <ol>
     *     <li>Đọc nội dung file HTML template từ thư mục <code>resources/templates</code>.</li>
     *     <li>Thay thế placeholder <code>[[OTP_CODE]]</code> bằng mã OTP thực tế.</li>
     *     <li>Tạo đối tượng {@link MimeMessage} và cấu hình tiêu đề, người nhận, và nội dung HTML.</li>
     *     <li>Gửi email qua {@link JavaMailSender}.</li>
     * </ol>
     *
     * @param email địa chỉ email của người nhận.
     * @param otp   mã OTP cần gửi đến người dùng.
     * @throws MessagingException nếu xảy ra lỗi khi tạo hoặc gửi email.
     * @throws IOException nếu không thể đọc file template hoặc gặp lỗi I/O khác.
     * @throws com.pm.authservice.exception.AppException nếu không tìm thấy file template email trong classpath.
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
