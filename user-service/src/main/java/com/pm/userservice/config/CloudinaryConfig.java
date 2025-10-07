package com.pm.userservice.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Cloudinary để upload, lưu trữ và quản lý ảnh.
 * <p>
 * Lấy các thông tin cấu hình từ application.properties hoặc application.yml:
 * <ul>
 *     <li>cloud_name</li>
 *     <li>api_key</li>
 *     <li>api_secret</li>
 * </ul>
 * </p>
 */
@Configuration
@ComponentScan(basePackages = "com.pm.userservice")
public class CloudinaryConfig {
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    /**
     * Tạo bean Cloudinary để sử dụng trong project.
     * <p>
     * Bean này có thể inject vào service hoặc component khác để upload ảnh.
     * </p>
     *
     * @return đối tượng Cloudinary đã cấu hình
     */
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
