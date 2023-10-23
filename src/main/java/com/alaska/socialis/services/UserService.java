package com.alaska.socialis.services;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.UserServiceInterface;

@Service
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Override
    public UserDetails fetchUserDetailsByUsername(String username) {
        UserDetails user = this.userRepository.findByUsername(username);
        return user;
    }

    @Override
    public UserDto updateUserCoverImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/user/cover_images",
                multipartFile);
        user.get().setCoverImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        return this.buildDto(updatedUser);

    }

    @Override
    public UserDto updateUserProfileImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/user/profile_images",
                multipartFile);
        user.get().setImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        return this.buildDto(updatedUser);

    }

    public UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).firstname(newUser.getFirstname()).lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .imageUrl(newUser.getImageUrl()).bio(newUser.getBio()).coverImageUrl(newUser.getCoverImageUrl())
                .phonenumber(newUser.getPhonenumber())
                .build();
    }
}
