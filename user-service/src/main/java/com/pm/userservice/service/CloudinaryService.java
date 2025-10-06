package com.pm.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


/**
 * Service xử lý upload file lên Cloudinary.
 * <p>
 * Cung cấp phương thức upload file và trả về URL công khai của file.
 * </p>
 */
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload file lên Cloudinary.
     *
     * @param file file cần upload (MultipartFile)
     * @return URL công khai (secure URL) của file vừa upload
     * @throws IOException nếu có lỗi khi đọc file hoặc upload
     */
    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));
        return uploadResult.get("secure_url").toString(); // URL công khai của file
    }
}
