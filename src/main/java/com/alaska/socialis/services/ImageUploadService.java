package com.alaska.socialis.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    public Map<String, Object> uploadImageToCloud(MultipartFile file) throws IOException {
        Map<String, String> params = ObjectUtils.asMap(
                "resource_type", "image",
                "public_id", "socialis/post/images",
                "folder", "socialis/post/images");

        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult;
    }
}
