package com.pm.userservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


/**
 * Service for handling file uploads to Cloudinary.
 * <p>
 * Provides methods to upload files and return their public URLs.
 * </p>
 */
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload a file to Cloudinary.
     *
     * @param file the file to be uploaded (MultipartFile)
     * @return the public URL (secure URL) of the uploaded file
     * @throws IOException if an error occurs while reading the file or uploading
     */
    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));
        return uploadResult.get("secure_url").toString(); // public URL
    }
}
