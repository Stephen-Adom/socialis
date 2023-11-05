package com.alaska.socialis.services;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.UserFollows;
import com.alaska.socialis.model.dto.UserSummaryDto;
import com.alaska.socialis.model.requestModel.UserInfoRequeset;
import com.alaska.socialis.repository.UserFollowsRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.UserServiceInterface;

@Service
public class UserService implements UserServiceInterface {

    private static final String COVER_IMAGE_CLOUD_PATH = "socialis/user/cover_images";
    private static final String PROFILE_IMAGE_CLOUD_PATH = "socialis/user/profile_images";
    private static final String UPDATE_LIVE_USER_URL = "/feed/user/update";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private UserFollowsRepository userFollowsRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public UserDetails fetchUserDetailsByUsername(String username) {
        UserDetails user = this.userRepository.findByUsername(username);
        return user;
    }

    @Override
    public void updateUserCoverImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getCoverImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud(COVER_IMAGE_CLOUD_PATH,
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage(COVER_IMAGE_CLOUD_PATH + "/", currentImage));
        }

        user.get().setCoverImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_URL, this.buildDto(updatedUser));

    }

    @Override
    public void updateUserProfileImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud(PROFILE_IMAGE_CLOUD_PATH,
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage(PROFILE_IMAGE_CLOUD_PATH + "/", currentImage));
        }

        user.get().setImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_URL, this.buildDto(updatedUser));

    }

    @Override
    public void updateUserInfo(Long userId, UserInfoRequeset requestBody, BindingResult validationResult)
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

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_URL, this.buildDto(updatedUser));
    }

    @Override
    public UserDto fetchUserInfoFullInformation(String username) throws EntityNotFoundException {
        User userInfo = (User) this.userRepository.findByUsername(username);
        if (Objects.isNull(userInfo)) {
            throw new EntityNotFoundException("User with username " + username + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        return this.buildDto(userInfo);
    }

    @Override
    public UserSummaryDto fetchUserInformationByUsername(String username) throws EntityNotFoundException {
        User userInfo = (User) this.userRepository.findByUsername(username);
        if (Objects.isNull(userInfo)) {
            throw new EntityNotFoundException("User with username " + username + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        return this.buildUserSummaryInfo(userInfo);
    }

    public UserSummaryDto buildUserSummaryInfo(User user) {
        // int totalPostCount = this.postRepository.countByUserId(user.getId());

        UserSummaryDto userInfo = new UserSummaryDto();
        userInfo.setFirstname(user.getFirstname());
        userInfo.setLastname(user.getLastname());
        userInfo.setImageUrl(user.getImageUrl());
        userInfo.setBio(user.getBio());
        userInfo.setUsername(user.getUsername());
        userInfo.setTotalPost(user.getNoOfPosts());
        userInfo.setFollowers(user.getNoOfFollowers());
        userInfo.setFollowing(user.getNoOfFollowing());
        return userInfo;
    }

    public UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).uid(newUser.getUid()).firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .imageUrl(newUser.getImageUrl()).bio(newUser.getBio()).coverImageUrl(newUser.getCoverImageUrl())
                .phonenumber(newUser.getPhonenumber()).address(newUser.getAddress())
                .noOfFollowers(newUser.getNoOfFollowers()).noOfPosts(newUser.getNoOfPosts())
                .noOfFollowing(newUser.getNoOfFollowing()).build();
    }

    @Override
    public void followUser(Long followerId, Long followingId) {
        Optional<User> follower = this.userRepository.findById(followerId);
        Optional<User> following = this.userRepository.findById(followingId);

        UserFollows userFollows = new UserFollows();
        follower.get().setNoOfFollowing(follower.get().getNoOfFollowing() + 1);
        following.get().setNoOfFollowers(following.get().getNoOfFollowers() + 1);
        userFollows.setFollower(follower.get());
        userFollows.setFollowing(following.get());

        userFollowsRepository.save(userFollows);

        Optional<User> followerUpdate = this.userRepository.findById(follower.get().getId());

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_URL, this.buildDto(followerUpdate.get()));
    }
}
