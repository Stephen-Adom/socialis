package com.alaska.socialis.services;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.requestModel.UserInfoRequeset;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.UserServiceInterface;

@Service
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public UserDetails fetchUserDetailsByUsername(String username) {
        UserDetails user = this.userRepository.findByUsername(username);
        return user;
    }

    @Override
    public UserDto updateUserCoverImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getCoverImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/user/cover_images",
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage("socialis/user/cover_images/", currentImage));
        }

        user.get().setCoverImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        return this.buildDto(updatedUser);

    }

    @Override
    public UserDto updateUserProfileImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/user/profile_images",
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage("socialis/user/profile_images/", currentImage));
        }

        user.get().setImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        return this.buildDto(updatedUser);

    }

    @Override
    public UserDto updateUserInfo(Long userId, UserInfoRequeset requestBody, BindingResult validationResult)
            throws EntityNotFoundException, ValidationErrorsException {
        Optional<User> user = this.userRepository.findById(userId);

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        User existingUser = user.get();

        if (!existingUser.getFirstname().equals(requestBody.getFirstname()) && !"".equals(requestBody.getFirstname())) {
            existingUser.setFirstname(requestBody.getFirstname());
        }

        if (!existingUser.getLastname().equals(requestBody.getLastname()) && !"".equals(requestBody.getLastname())) {
            existingUser.setLastname(requestBody.getLastname());
        }

        if (!existingUser.getUsername().equals(requestBody.getUsername()) && !"".equals(requestBody.getUsername())) {
            existingUser.setUsername(requestBody.getUsername());
        }

        existingUser.setPhonenumber(requestBody.getPhonenumber());

        existingUser.setAddress(requestBody.getAddress());

        existingUser.setBio(requestBody.getBio());

        User updatedUser = this.userRepository.save(existingUser);

        return this.buildDto(updatedUser);
    }

    public UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).uid(newUser.getUid()).firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .imageUrl(newUser.getImageUrl()).bio(newUser.getBio()).coverImageUrl(newUser.getCoverImageUrl())
                .phonenumber(newUser.getPhonenumber()).address(newUser.getAddress())
                .build();
    }
}
