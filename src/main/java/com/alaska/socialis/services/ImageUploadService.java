package com.alaska.socialis.services;

import java.io.IOException;
import java.io.InputStream;
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

    public Map<String, Object> uploadImageToCloud(String path, MultipartFile file, String resourceType)
            throws IOException {
        Map<String, String> params = ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", path);

        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult;
    }

    public Map<String, Object> uploadStoriesToCloud(String path, String file, String resourceType)
            throws IOException {
        Map<String, String> params = ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", path);

        Map<String, Object> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult;
    }

    public void deleteUploadedImage(String delimiter, String imageUrl) {
        try {
            String[] imagUrlArray = imageUrl.split(delimiter);
            String imageName = imagUrlArray[1].split("\\.")[0];
            String public_id = delimiter + imageName;
            System.out.println("================================ delete image ======================================");
            System.out.println(public_id);
            this.cloudinary.uploader().destroy(public_id, null);
        } catch (IOException e) {
            System.out.println("An error has occured: " + e);
        }
    }
}
